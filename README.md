# Pioneer Delivery — Part I

Pioneer Delivery is a Java 25 desktop application for fixed-interval delivery scheduling. Its Operator Workbench loads one CSV dataset and shows the results of Tasks A–C together. Task D is a separate written deliverable and is outside this application.

## Build and run

Install JDK 25 and set `JAVA_HOME` to it. On Linux or macOS, launch from the repository root with:

```sh
./run.sh
```

On Windows, run `mvnw.cmd javafx:run` instead. The Maven wrapper downloads Maven 3.9.11 on first use, so a separate Maven installation is unnecessary; the first run needs an internet connection. To run the tests, use `./mvnw clean verify` (or `mvnw.cmd clean verify` on Windows). The launcher opens the JavaFX window in a graphical session; choose a file from `data/` to explore. The project uses JavaFX 25.0.2 and JUnit 5.11.4, which Maven resolves on the first build.

## Input and example data

Input is a headerless `.csv` file with one delivery per line:

```text
id,start_time,finish_time
```

The line above describes the fields; do not put it in the file as a header. For example, `D02,2,5` means delivery D02 occupies a driver from minute offset 2 until 5. IDs must be nonblank and unique within the file. Times are nonnegative integers with `start_time < finish_time`; a delivery may begin exactly when another ends. Input order may be arbitrary. Surrounding whitespace is trimmed, and quoted CSV IDs may contain commas or escaped quotes.

The chooser offers `.csv` files, and the reader independently rejects other filename extensions. Invalid rows are skipped while valid rows continue to load. The workbench reports total nonblank, accepted, and skipped row counts. If rows are skipped, the status states that results use accepted records only and gives the actual diagnostic log path. A missing, unreadable, or non-CSV file is a load error; an opened empty CSV file is a valid empty dataset.

The files in `data/` exercise different outcomes. Counts are based on accepted records only:

| File | Scenario | Accepted / skipped | Task A selected | Task B drivers | Task C rejected |
| --- | --- | ---: | ---: | ---: | ---: |
| [sample-deliveries.csv](data/sample-deliveries.csv) | Unsorted baseline | 7 / 0 | 3 | 3 | 4 |
| [back-to-back.csv](data/back-to-back.csv) | Shared endpoints; one driver handles all requests | 7 / 0 | 7 | 1 | 0 |
| [rush-hour.csv](data/rush-hour.csv) | Dense overlaps requiring several drivers | 12 / 0 | 3 | 4 | 9 |
| [mixed-input.csv](data/mixed-input.csv) | Invalid times, duplicate and blank IDs, extra field | 5 / 5 | 4 | 2 | 1 |

Loading `mixed-input.csv` also shows the skipped-row diagnostic log path in the workbench.

## Program organization

| Location | Responsibility |
| --- | --- |
| `src/main/java/edu/pioneer/model` | Immutable `Delivery` records and per-driver schedules |
| `src/main/java/edu/pioneer/input` | CSV validation, accepted dataset/report, and skipped-row diagnostics |
| `src/main/java/edu/pioneer/scheduling` | Independent Task A, B, and C operations over accepted deliveries |
| `src/main/java/edu/pioneer/ui` and `App.java` | JavaFX Operator Workbench and application entry point |
| `src/test/java/edu/pioneer` | Model, file-reader, scheduler, and file-to-scheduler tests |

The schedulers do not read files, write logs, or depend on JavaFX. The file reader creates the accepted dataset, which all three tasks use.

## Algorithms and why they are optimal

**Task A — maximum deliveries for one driver.** Copy the accepted deliveries, sort by increasing finish time, and repeatedly select the earliest-finishing delivery whose start is at or after the last selected finish. For any optimal schedule, its first delivery can be replaced by the greedy first choice: the greedy choice finishes no later, so every later delivery remains possible. Applying that exchange repeatedly proves the selected count is maximum. Sorting dominates the scan: `O(n log n)` time and `O(n)` extra space.

**Task B — minimum drivers for all deliveries.** Copy and sort deliveries by increasing start time. A min-heap stores each current driver keyed by the finish time of their last assignment. For a new delivery, reuse the driver at the heap head if that driver has finished; otherwise create a driver. If even the earliest-finishing driver is still occupied, every existing driver overlaps the new delivery at its start, so an additional driver is necessary. The algorithm therefore never creates an avoidable driver. Sorting and heap operations take `O(n log n)` time and `O(n)` extra space. Each accepted delivery is assigned exactly once.

**Task C — minimum rejections for one driver.** If there are `n` accepted deliveries and Task A selects `k`, return `n - k`. No one-driver solution can retain more than `k`, and Task A demonstrates a solution retaining `k`, so this rejection count is minimum. Task C calls Task A once: `O(n log n)` time and `O(n)` extra space. Skipped CSV rows are not algorithmic rejections.

## Required case results

The following are the course assignment's six cases for each task. These observed counts are asserted by `TaskASchedulerTest`, `TaskBSchedulerTest`, and `TaskCSchedulerTest` and passed in the verification run below. Where multiple optimal schedules exist, the tests check the count and schedule validity without requiring one particular ID selection or driver mapping.

### Task A — selected deliveries

| Case | Input intervals | Required | Observed |
| ---: | --- | ---: | ---: |
| 1 | No deliveries | 0 | 0 |
| 2 | `(1, 3)` | 1 | 1 |
| 3 | `(1, 3), (3, 5), (5, 7)` | 3 | 3 |
| 4 | `(1, 5), (2, 6), (3, 7)` | 1 | 1 |
| 5 | `(5, 7), (1, 3), (3, 5), (2, 4)` | 3 | 3 |
| 6 | `(1, 4), (2, 4), (4, 6)` | 2 | 2 |

### Task B — minimum drivers

| Case | Input intervals | Required | Observed |
| ---: | --- | ---: | ---: |
| 1 | No deliveries | 0 | 0 |
| 2 | `(1, 3)` | 1 | 1 |
| 3 | `(1, 3), (3, 5), (5, 7)` | 1 | 1 |
| 4 | `(1, 5), (2, 6), (3, 7)` | 3 | 3 |
| 5 | `(1, 4), (2, 5), (4, 7), (5, 8)` | 2 | 2 |
| 6 | `(5, 8), (1, 4), (2, 5), (4, 6), (6, 9)` | 2 | 2 |

### Task C — minimum rejected deliveries

| Case | Input intervals | Required | Observed |
| ---: | --- | ---: | ---: |
| 1 | No deliveries | 0 | 0 |
| 2 | `(1, 3)` | 0 | 0 |
| 3 | `(1, 3), (3, 5), (5, 7)` | 0 | 0 |
| 4 | `(1, 5), (2, 6), (3, 7)` | 2 | 2 |
| 5 | `(5, 7), (1, 3), (3, 5), (2, 4)` | 1 | 1 |
| 6 | `(1, 2), (2, 3), (3, 4), (1, 3)` | 1 | 1 |

## Verification evidence

On October 13, 2026, `./mvnw clean verify` completed with Eclipse Temurin JDK 25.0.4.1 and Maven 3.9.11: **48 tests, 0 failures, 0 errors, 0 skipped**. Besides the required cases, these tests cover empty and malformed CSV files, duplicate IDs, non-CSV rejection, diagnostic counts, shared endpoints, equal finish times, unchanged accepted records, compatible schedules, and exactly-once Task B assignment. `SubmissionIntegrationTest` loads the baseline sample file and checks all three results; its mixed-row case confirms that skipped rows never enter any scheduler and that Task C uses the accepted count. The counts for the other files in `data/` were checked by calling the same reader and schedulers directly.

With the same JDK, `./run.sh` reached JavaFX startup and Maven exited successfully in a graphical session. This is a launch check, not an automated visual UI test.
