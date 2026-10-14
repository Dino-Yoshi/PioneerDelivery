# Pioneer Delivery — Software Design Specification

- **Project:** Pioneer Delivery Company's Algorithm Portfolio — Part I
- **Version:** 1.0
- **Date:** October 12, 2026
- **Status:** Accepted design baseline for implementation planning
- **Requirements authority:** [SRS-Pioneer-Delivery-v1.0.md](SRS-Pioneer-Delivery-v1.0.md)

## 1. Design goals and baseline

The system separates file processing, scheduling, and presentation. The scheduling operations receive accepted `Delivery` objects and return results without reading files, writing logs, or updating JavaFX controls.

| Decision | Accepted design |
|---|---|
| Java | JDK 25 |
| Build | Maven |
| GUI | JavaFX |
| Shared record | Immutable `Delivery` |
| Driver association | Mutable `Driver` aggregates immutable Delivery references |
| Task A | Earliest compatible finish time |
| Task B | Ascending start time plus finish-time min-heap |
| Task C | Reuse Task A and return `n - k` |

## 2. Conceptual module structure

```text
edu.pioneer
├── App
├── model
│   ├── Delivery
│   └── Driver
├── input
│   ├── DeliveryFileReader
│   ├── ProcessingReport
│   └── DiagnosticLogger
├── scheduling
│   ├── TaskAScheduler
│   ├── TaskBScheduler
│   └── TaskCScheduler
└── ui
    └── MainWindow
```

This is a responsibility boundary, not a final class-name mandate. The GUI and tests call the same scheduling interfaces.

## 3. Shared object contracts

### 3.1 Delivery

```text
Delivery(String id, int startTime, int finishTime)
String getId()
int getStartTime()
int getFinishTime()
```

`Delivery` is final and immutable. Its constructor rejects null or blank IDs and intervals that do not satisfy `0 <= startTime < finishTime`. It preserves the source ID and has no generated ID or setters.

### 3.2 Driver

```text
Driver()
int getId()
List<Delivery> getDeliveries()
int getFinishTime()
void assignDelivery(Delivery delivery)
```

Each Driver receives a class-local integer ID beginning at 1 for the application run. IDs are labels, not the driver count, and are not reset between scheduling calls during one run. The driver owns its schedule list and exposes an unmodifiable view. `assignDelivery` validates compatibility before appending and updating `finishTime` together.

## 4. Scheduling interfaces

```text
List<Delivery> selectDeliveries(List<Delivery> deliveries)
List<Driver> assignDrivers(List<Delivery> deliveries)
int countRejectedDeliveries(List<Delivery> deliveries)
```

The input list is non-null, contains non-null accepted records, and may be in arbitrary order. Empty input is valid. Each operation preserves the input list and Delivery objects.

## 5. Task A design

Copy the accepted list, sort the copy by ascending finish time, and scan it once. Keep a delivery when its start time is greater than or equal to the last selected finish time.

```text
selectDeliveries(deliveries):
    ordered = copy(deliveries)
    sort ordered by finishTime ascending
    selected = empty list
    lastFinish = 0

    for delivery in ordered:
        if delivery.startTime >= lastFinish:
            selected.add(delivery)
            lastFinish = delivery.finishTime

    return selected
```

The earliest-finishing compatible delivery leaves the most remaining time for later requests. An exchange argument shows it can replace the first delivery of an optimal schedule without reducing the number of future choices. The sort dominates the scan, giving `O(n log n)` time and `O(n)` additional space.

## 6. Task B design

Copy the accepted list and sort by ascending start time. Maintain:

- a result list containing every created Driver;
- a min-heap containing the same Driver objects, ordered by `finishTime`;
- each Driver's assigned Delivery references.

```text
assignDrivers(deliveries):
    ordered = copy(deliveries)
    sort ordered by startTime ascending
    drivers = empty list
    heap = empty min-heap ordered by finishTime

    for delivery in ordered:
        if heap is not empty and heap.peek().finishTime <= delivery.startTime:
            driver = heap.poll()
        else:
            driver = new Driver()
            drivers.add(driver)

        driver.assignDelivery(delivery)
        heap.add(driver)

    return drivers
```

The heap head is the earliest available driver. If that driver is still busy, every other driver is also busy, so a new driver is unavoidable. Removing a reused driver before changing its finish time and reinserting it preserves heap ordering. Sorting and heap operations give `O(n log n)` time and `O(n)` additional space.

## 7. Task C design

Task C calls Task A once and subtracts the selected count from the accepted-record count:

```text
countRejectedDeliveries(deliveries):
    selected = selectDeliveries(deliveries)
    return deliveries.size() - selected.size()
```

If Task A retains the maximum compatible subset of size `k`, then every valid one-driver solution must reject at least `n - k` records, and Task A demonstrates a solution rejecting exactly that many. Therefore the result is minimum. Task C remains `O(n log n)` because Task A performs the only sort.

## 8. Input and presentation boundary

The input layer reads headerless CSV, validates rows, records accepted/skipped counts, and writes diagnostics. It passes only accepted Delivery objects to scheduling. The file chooser filters for `.csv` files, and the input boundary independently rejects a filename without a `.csv` extension (case-insensitive) before parsing rows. The extension check does not replace row validation for files bearing `.csv` names.

The JavaFX layer holds one accepted dataset. After a successful load, it computes and shows all three task results on one page. When rows are skipped, the processing context identifies the diagnostic log's actual location. After a file-open failure, read failure, or non-CSV rejection, the JavaFX layer clears the previously accepted dataset, replaces the three results with an unavailable state, and displays the error. Scheduling resumes only after another CSV file loads successfully.

### 8.1 Scheduling Workbench layout

The approved desktop UI is the Operator Workbench. Its window uses a left sidebar and a scrollable main content area. The sidebar shows the application name, current file, and processing counts, with no navigation controls or `CSV files only` tagline. All results remain on the main page.

The main area begins with `Scheduling Workbench`, the title `Delivery Analysis`, a `Choose CSV file…` control, and the processing status. Below it, three result sections appear in Task A, B, C order. Each section places its `Task A`, `Task B`, or `Task C` label above its title:

1. `Maximum Deliveries for One Driver`
2. `Minimum Drivers to Complete All Deliveries`
3. `Deliveries Rejected for One Driver`

Task A shows its selected count and delivery table. Task B shows its minimum driver count and each driver's schedule. Task C shows its count and describes it as deliveries to reject when only one driver is available. A zero Task C result also displays `No deliveries to reject.` Empty-file results use the corresponding zero counts and plain-language empty messages.

The visual style follows the selected Workbench prototype: a dark slate sidebar (`#1d3041`) occupying about one quarter of the desktop width, a light neutral main background (`#f6f8fa`), white result sections, teal primary controls (`#087a71`), a readable sans-serif font, and prominent numeric counts. The exact JavaFX scene graph, threading, and packaging remain implementation decisions.

## 9. Verification design

Use unit tests for Delivery and Driver contracts, scheduler tests for all required A–C cases, and focused file-reader integration tests for CSV validation, non-CSV rejection, and skipped-row reporting. Tests shall not require a particular optimal ID set or driver mapping when multiple valid results exist. They shall validate counts, compatibility, exactly-once assignment, immutability, and accepted-record boundaries.

## 10. Deferred implementation choices

The following were intentionally left open by this design baseline: exact package/class names, Maven plugins and test framework, JavaFX control structure, parser/logging implementation, direct-call exception details, and distribution format. Task D remains outside the A–C application design; its separate written answer is in [TaskD.md](TaskD.md).
