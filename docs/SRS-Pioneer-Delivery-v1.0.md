# Pioneer Delivery — Software Requirements Specification

- **Project:** Pioneer Delivery Company's Algorithm Portfolio — Part I: Greedy Algorithms
- **Course:** CS 413 — Analysis of Algorithms
- **Version:** 1.0
- **Date:** October 12, 2026
- **Status:** Accepted team baseline for implementation planning
- **Supersedes:** Parent SRS v0.7 and Task A v0.4, Task B v0.1, and Task C v0.1

## 1. Purpose and scope

Pioneer Delivery receives delivery requests, each occupying a driver during a fixed time interval. The system shall read delivery requests from a file and support three fixed-interval scheduling operations:

- **Task A:** maximize the number of deliveries completed by one driver.
- **Task B:** accept every valid delivery using the minimum number of drivers.
- **Task C:** determine the minimum number of deliveries to reject so one driver can perform the rest.

Task D is a separate written counterexample involving flexible one-unit deliveries, ready times, and profits. It remains deferred from the first application workflow.

The application will be a Java desktop GUI. Tasks A–C shall remain usable independently of the GUI so their behavior can be tested directly.

## 2. Accepted implementation baseline

| Decision | Accepted choice |
|---|---|
| Language/runtime | JDK 25 |
| Build | Maven |
| GUI | JavaFX |
| Input | Headerless comma-delimited CSV |
| Shared data | One validated accepted dataset for Tasks A–C |
| Time representation | Nonnegative integer minute offsets; no 24-hour rollover |

These are team decisions, not additional instructor requirements unless explicitly stated below.

## 3. Shared data and input requirements

### 3.1 Delivery records

Each delivery shall contain:

| Field | Requirement |
|---|---|
| `id` | Nonblank source identifier; unique within one input dataset |
| `start_time` | Integer satisfying `start_time >= 0` |
| `finish_time` | Integer satisfying `start_time < finish_time` |

The ID, start time, and finish time shall remain associated when records are reordered. Accepted records shall not be modified by scheduling operations.

### 3.2 File processing

- The file shall be headerless CSV with exactly `id,start_time,finish_time` fields per record.
- Incidental surrounding whitespace may be trimmed.
- Input order may be arbitrary.
- Invalid rows shall be skipped, logged, and followed by continued processing of valid rows.
- Invalid rows shall not be counted as Task C algorithmic rejections.
- A successfully opened empty file is valid and produces zero-sized results.
- A file-open or read failure is an error, not an empty dataset.
- After a file-open or read failure, the application shall clear any previously accepted dataset, disable scheduling until a file loads successfully, and show the error.
- The processing report shall show total nonblank rows, accepted records, and skipped records.
- Each skipped-row log entry shall include the source filename, line number, reason, readable ID when available, and character position when reasonably identifiable.
- When rows are skipped, the user-facing summary shall state that results use accepted records only and show where to find the diagnostic log.

### 3.3 Scheduling rules

- A driver may not perform overlapping deliveries.
- A delivery may start exactly when the previous delivery finishes.
- A valid delivery may be assigned at most once in Task B.
- Scheduling shall preserve the original accepted records.

## 4. Task A requirements — maximum deliveries for one driver

Given the accepted deliveries, select a maximum-cardinality mutually compatible subset for one driver.

The result shall show each selected delivery's ID, start time, and finish time, plus the total selected count. The displayed order shall be compatible, and the count shall equal the number of displayed deliveries. Any maximum-count schedule is acceptable, but repeated runs on the same input shall be consistent.

The scheduling operation shall run in `O(n log n)` time, including sorting. The README shall explain the greedy choice, its optimality, and its running time.

Required expected counts:

| Test | Intervals | Count |
|---:|---|---:|
| 1 | No deliveries | 0 |
| 2 | `(1, 3)` | 1 |
| 3 | `(1, 3), (3, 5), (5, 7)` | 3 |
| 4 | `(1, 5), (2, 6), (3, 7)` | 1 |
| 5 | `(5, 7), (1, 3), (3, 5), (2, 4)` | 3 |
| 6 | `(1, 4), (2, 4), (4, 6)` | 2 |

## 5. Task B requirements — minimum number of drivers

Given the accepted deliveries, assign every delivery exactly once to a driver so that no driver's schedule overlaps and the number of drivers is minimum.

The result shall show each driver's assigned delivery schedule and the minimum driver count. Any conflict-free minimum assignment is acceptable. With no accepted deliveries, the result shall show zero drivers.

The implementation shall use a priority queue/min-heap and run in `O(n log n)` time, including sorting. The README shall explain the heap contents, driver selection, minimum-driver reasoning, and running time.

Required expected driver counts:

| Test | Intervals | Drivers |
|---:|---|---:|
| 1 | No deliveries | 0 |
| 2 | `(1, 3)` | 1 |
| 3 | `(1, 3), (3, 5), (5, 7)` | 1 |
| 4 | `(1, 5), (2, 6), (3, 7)` | 3 |
| 5 | `(1, 4), (2, 5), (4, 7), (5, 8)` | 2 |
| 6 | `(5, 8), (1, 4), (2, 5), (4, 6), (6, 9)` | 2 |

## 6. Task C requirements — minimum rejected deliveries

Given `n` accepted deliveries, determine the minimum number that must be rejected so the remaining deliveries are mutually compatible for one driver.

If Task A's maximum compatible subset has size `k`, Task C shall return `n - k`. The result shall display a nonnegative integer and shall not count invalid input rows.

The operation shall run in `O(n log n)` time, including any sorting. The README shall explain its relationship to Task A, the `n - k` calculation, and its running time.

Required expected rejection counts:

| Test | Intervals | Rejections |
|---:|---|---:|
| 1 | No deliveries | 0 |
| 2 | `(1, 3)` | 0 |
| 3 | `(1, 3), (3, 5), (5, 7)` | 0 |
| 4 | `(1, 5), (2, 6), (3, 7)` | 2 |
| 5 | `(5, 7), (1, 3), (3, 5), (2, 4)` | 1 |
| 6 | `(1, 2), (2, 3), (3, 4), (1, 3)` | 1 |

## 7. Verification requirements

The implementation shall test all required cases for Tasks A–C, including empty input, unordered input, shared endpoints, overlapping intervals, and equal finish times. Tests shall also verify:

- malformed rows are skipped and logged;
- scheduling uses accepted records only;
- IDs and interval fields remain associated and unchanged;
- Task B assigns every accepted delivery exactly once;
- no driver schedule overlaps;
- Task C equals accepted-count minus Task A's selected count.

No test results are claimed until executable code exists.

## 8. Submission requirements

The final submission shall include source code, a data file, and `README.md`. The README shall include the language, build/run instructions, input format, test data, program organization, algorithm explanations for Tasks A–C, and required test results.

## 9. Deferred scope

The following are implementation-planning decisions, not changes to the accepted requirements: exact Java package placement, parser/logging classes, JavaFX screen layout, threading, packaging details, test framework, and direct-call exception policy.

Task D remains deferred as a separate written counterexample and is not part of the first A–C GUI workflow.
