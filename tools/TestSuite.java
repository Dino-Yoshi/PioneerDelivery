import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Runs the full Maven suite and presents its Surefire results without build noise. */
public final class TestSuite {
    private static final Path projectDirectory = Path.of("").toAbsolutePath().normalize();
    private static final Path targetDirectory = projectDirectory.resolve("target");
    private static final Pattern requiredCasePattern = Pattern.compile("task([ABC])CsvMatchesRequiredCase\\(.*\\)\\[([1-6])\\]");

    private TestSuite() {
    }

    public static void main(String[] args) {
        if (args.length != 0) {
            System.err.println("Usage: run-tests.sh or run-tests.cmd (no arguments)");
            System.exit(2);
        }

        System.out.println("Running the Pioneer Delivery test suite...");
        try {
            BuildResult build = runMaven();
            List<TestResult> tests = readCurrentResults(build.startedAt());
            Report report = formatReport(build, tests);
            Files.writeString(targetDirectory.resolve("test-results.txt"), report.text());
            System.out.print(report.text());
            if (!report.passed()) {
                System.exit(1);
            }
        } catch (Exception failure) {
            if (failure instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.err.println("Could not complete the test run: " + failure.getMessage());
            System.err.println("Check that JDK 25 is installed and JAVA_HOME points to it.");
            System.err.println("Maven log, if created: target/test-build.log");
            System.exit(1);
        }
    }

    private static BuildResult runMaven() throws IOException, InterruptedException {
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");
        Path wrapper = projectDirectory.resolve(windows ? "mvnw.cmd" : "mvnw");
        List<String> command = windows
                ? List.of("cmd.exe", "/d", "/c", "call \"" + wrapper + "\" -q clean verify")
                : List.of(wrapper.toString(), "-q", "clean", "verify");
        Instant startedAt = Instant.now();
        Process process = new ProcessBuilder(command).directory(projectDirectory.toFile()).redirectErrorStream(true).start();
        byte[] output;
        try (InputStream stream = process.getInputStream()) {
            output = stream.readAllBytes();
        }
        int exitCode = process.waitFor();
        Files.createDirectories(targetDirectory);
        Files.write(targetDirectory.resolve("test-build.log"), output);
        return new BuildResult(exitCode, startedAt, new String(output, StandardCharsets.UTF_8));
    }

    private static List<TestResult> readCurrentResults(Instant startedAt) throws Exception {
        Path reportsDirectory = targetDirectory.resolve("surefire-reports");
        if (!Files.isDirectory(reportsDirectory)) {
            return List.of();
        }

        List<Path> files;
        try (Stream<Path> paths = Files.list(reportsDirectory)) {
            files = paths.filter(path -> path.getFileName().toString().startsWith("TEST-"))
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .sorted()
                    .toList();
        }

        List<TestResult> tests = new ArrayList<>();
        for (Path file : files) {
            // A failed build must never display stale results from an earlier run.
            if (!Files.getLastModifiedTime(file).toInstant().isBefore(startedAt)) {
                tests.addAll(readXmlReport(file));
            }
        }
        return tests;
    }

    private static List<TestResult> readXmlReport(Path file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Element suite = factory.newDocumentBuilder().parse(file.toFile()).getDocumentElement();
        if (!"testsuite".equals(suite.getTagName())) {
            throw new IOException("Not a Surefire test report: " + file);
        }

        List<TestResult> tests = new ArrayList<>();
        NodeList cases = suite.getElementsByTagName("testcase");
        for (int index = 0; index < cases.getLength(); index++) {
            Element test = (Element) cases.item(index);
            Status status = Status.PASS;
            String detail = "";
            for (Node child = test.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child instanceof Element outcome) {
                    String tag = outcome.getTagName();
                    if ("failure".equals(tag) || "error".equals(tag) || "skipped".equals(tag)) {
                        status = switch (tag) {
                            case "failure" -> Status.FAIL;
                            case "error" -> Status.ERROR;
                            default -> Status.SKIP;
                        };
                        detail = outcome.getAttribute("message");
                        if (detail.isBlank()) {
                            detail = outcome.getTextContent().strip();
                        }
                    }
                }
            }
            tests.add(new TestResult(suite.getAttribute("name"), test.getAttribute("name"), status, detail));
        }
        return tests;
    }

    private static Report formatReport(BuildResult build, List<TestResult> tests) {
        List<TestResult> required = tests.stream().filter(test -> requiredCase(test) != null)
                .sorted(Comparator.comparing((TestResult test) -> requiredCase(test).task())
                        .thenComparingInt(test -> requiredCase(test).number())).toList();
        Map<String, List<TestResult>> other = new TreeMap<>();
        for (TestResult test : tests) {
            if (requiredCase(test) == null) {
                other.computeIfAbsent(test.suite(), unused -> new ArrayList<>()).add(test);
            }
        }

        StringBuilder output = new StringBuilder("\nPioneer Delivery test results\n\n");
        output.append("Required CSV cases (18 expected)\n");
        for (TestResult test : required) {
            RequiredCase requiredCase = requiredCase(test);
            output.append("  ").append(test.status()).append("  Task ").append(requiredCase.task())
                    .append(" case ").append(requiredCase.number()).append("  [data/")
                    .append(requiredCase.task()).append("TestValues/case-").append(requiredCase.number()).append(".csv]\n");
            appendFailure(output, test);
        }
        if (required.isEmpty()) {
            output.append("  No required CSV cases were recorded.\n");
        }

        output.append("\nOther tests\n");
        for (Map.Entry<String, List<TestResult>> entry : other.entrySet()) {
            output.append("  ").append(splitIdentifier(simpleName(entry.getKey())).replaceAll(" Test$", ""))
                    .append(" (").append(entry.getValue().size()).append(" tests)\n");
            entry.getValue().sort(Comparator.comparing(TestResult::name));
            for (TestResult test : entry.getValue()) {
                output.append("    ").append(test.status()).append("  ").append(humanizeMethod(test.name())).append('\n');
                appendFailure(output, test);
            }
        }
        if (other.isEmpty()) {
            output.append("  No other tests were recorded.\n");
        }

        long passed = tests.stream().filter(test -> test.status() == Status.PASS).count();
        long failed = tests.stream().filter(test -> test.status() == Status.FAIL).count();
        long errors = tests.stream().filter(test -> test.status() == Status.ERROR).count();
        long skipped = tests.stream().filter(test -> test.status() == Status.SKIP).count();
        List<String> missing = missingRequiredCases(required);
        boolean success = build.exitCode() == 0 && !tests.isEmpty() && failed == 0 && errors == 0 && skipped == 0 && missing.isEmpty();
        output.append("\nSummary: ").append(passed).append(" passed, ").append(failed).append(" failed, ")
                .append(errors).append(" errors, ").append(skipped).append(" skipped\n");
        if (!missing.isEmpty()) {
            output.append("Missing required cases: ").append(String.join(", ", missing)).append('\n');
        }
        if (build.exitCode() != 0 && failed == 0 && errors == 0) {
            output.append("Build did not complete successfully.\n");
            build.output().lines().filter(line -> line.contains("[ERROR]")).limit(8)
                    .forEach(line -> output.append("  ").append(line).append('\n'));
        }
        output.append("Result: ").append(success ? "PASS" : "FAIL").append('\n');
        output.append("Full report: target/test-results.txt\n");
        output.append("Maven log: target/test-build.log\n");
        return new Report(output.toString(), success);
    }

    private static void appendFailure(StringBuilder output, TestResult test) {
        if (test.status() != Status.PASS && !test.detail().isBlank()) {
            output.append("      ").append(test.detail().lines().findFirst().orElse("")).append('\n');
        }
    }

    private static RequiredCase requiredCase(TestResult test) {
        if (!test.suite().endsWith(".RequiredCaseCsvTest")) {
            return null;
        }
        Matcher match = requiredCasePattern.matcher(test.name());
        return match.matches() ? new RequiredCase(match.group(1).charAt(0), Integer.parseInt(match.group(2))) : null;
    }

    private static List<String> missingRequiredCases(List<TestResult> required) {
        Set<RequiredCase> found = new HashSet<>();
        required.forEach(test -> found.add(requiredCase(test)));
        List<String> missing = new ArrayList<>();
        for (char task : new char[]{'A', 'B', 'C'}) {
            for (int caseNumber = 1; caseNumber <= 6; caseNumber++) {
                if (!found.contains(new RequiredCase(task, caseNumber))) {
                    missing.add("Task " + task + " case " + caseNumber);
                }
            }
        }
        return missing;
    }

    private static String simpleName(String qualifiedName) {
        return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    }

    private static String humanizeMethod(String name) {
        int signature = name.indexOf('(');
        String method = signature < 0 ? name : name.substring(0, signature);
        String words = splitIdentifier(method).toLowerCase(Locale.ROOT);
        String label = Character.toUpperCase(words.charAt(0)) + words.substring(1);
        label = label.replaceAll("\\bcsv\\b", "CSV").replaceAll("\\bid\\b", "ID");
        label = label.replaceAll("\\bids\\b", "IDs").replaceAll("\\btask a\\b", "Task A");
        int variantStart = name.lastIndexOf('[');
        if (variantStart > signature && name.endsWith("]")) {
            label += " (variant " + name.substring(variantStart + 1, name.length() - 1) + ")";
        }
        return label;
    }

    private static String splitIdentifier(String identifier) {
        return identifier.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2")
                .replaceAll("([a-z0-9])([A-Z])", "$1 $2");
    }

    private enum Status { PASS, FAIL, ERROR, SKIP }

    private record BuildResult(int exitCode, Instant startedAt, String output) { }

    private record TestResult(String suite, String name, Status status, String detail) { }

    private record RequiredCase(char task, int number) { }

    private record Report(String text, boolean passed) { }
}
