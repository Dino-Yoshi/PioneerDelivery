# Pioneer Delivery — Software Requirements Specification (SRS)

**Project:** Pioneer Delivery Company's Algorithm Portfolio — Part I: Greedy Algorithms  
**Course:** CS 413 — Analysis of Algorithms  
**Version:** 0.2  
**Date:** October 8, 2026  
**Status:** Reviewed project-wide baseline; task-specific requirements pending

> **Scope of this version:** This overarching SRS covers the assignment's **Project Overview** and **Delivery Data** sections, plus project-level engineering decisions resolved during requirements review. It does **not** define the detailed requirements, outputs, algorithms, complexity constraints, or tests for Tasks A–D. Each task will receive its own linked SRS after a broad task-level scope review. No implementation architecture or code has been approved.

## Table of Contents

- [Revision History](#revision-history)

- [1. Introduction](#1-introduction)

  - [1.1 Purpose](#11-purpose)

  - [1.2 Business Context](#12-business-context)

  - [1.3 System Scope](#13-system-scope)

  - [1.4 Requirement Classification](#14-requirement-classification)

- [2. High-Level Functional Requirements](#2-high-level-functional-requirements)

- [3. Delivery Data Requirements](#3-delivery-data-requirements)

  - [3.1 Minimum Delivery Record](#31-minimum-delivery-record)

  - [3.2 Illustrative Source Data](#32-illustrative-source-data)

  - [3.3 Selected Time Interpretation](#33-selected-time-interpretation)

- [4. Scheduling Rules and Data Invariants](#4-scheduling-rules-and-data-invariants)

- [5. Input Contract and Error Handling](#5-input-contract-and-error-handling)

  - [5.1 File Format](#51-file-format)

  - [5.2 Record Processing](#52-record-processing)

  - [5.3 Processing Report and Diagnostics](#53-processing-report-and-diagnostics)

- [6. Engineering Decisions](#6-engineering-decisions)

- [7. Resolution of Open Questions](#7-resolution-of-open-questions)

- [8. Deferred Scope and Next Review](#8-deferred-scope-and-next-review)

- [9. Source and Traceability](#9-source-and-traceability)

## Revision History

| Version | Date | Description |
| - | - | - |
| **0.1** | 2026-10-08 | Established preliminary project-wide functional requirements, delivery data, business rules, and six open questions. Deferred task-level review. |
| **0.2** | 2026-10-08 | Resolved OQ-01 through OQ-06: unique IDs, arbitrary input order, headerless CSV, logged rejection of invalid records with partial processing, valid empty input, and nonnegative integer minute offsets without an application-specific maximum. Added a table of contents, explicit revision history, a concrete input contract, and a task-SRS boundary. |


## 1. Introduction

### 1.1 Purpose

Define and maintain traceable project-wide requirements for the first version of Pioneer Delivery's scheduling system. Separate **assignment-mandated behavior** from **team-chosen conventions and safeguards** so future design work does not accidentally treat optional decisions as instructor requirements.

### 1.2 Business Context

Pioneer Delivery receives daily delivery requests. Each request occupies one driver over a time interval, and a driver cannot carry out overlapping deliveries. The company wants to understand single-driver capacity and the workforce needed to accommodate all requests.

### 1.3 System Scope

The first version shall read a collection of requests from a file and address two business questions:

1. What is the maximum number of mutually compatible deliveries one driver can complete?

2. What is the minimum number of drivers required to complete all delivery requests without conflicts?

This document establishes **shared system expectations**, not the detailed behavior of each assignment task. The broader semester portfolio may later add divide-and-conquer, dynamic-programming, and graph-algorithm problems; those future parts are out of scope here.

### 1.4 Requirement Classification

- **Explicit:** Directly stated in the assignment's Project Overview or Delivery Data sections.

- **Team decision:** An agreed rule or convention not prescribed by those sections; changeable if later task requirements demand it.

- **Invariant:** A property the scheduling system must preserve to keep its data meaningful.

- **Deferred:** Will be analyzed separately rather than presumed here.

## 2. High-Level Functional Requirements

| ID | Requirement | Basis | Status |
| - | - | - | - |
| **FR-01** | The system **shall read** a collection of delivery requests from an input file. | Delivery Data, p. 2 | Explicit |
| **FR-02** | The system **shall determine** the maximum number of mutually compatible delivery requests that one driver can complete. | Project Overview, p. 1 | Explicit |
| **FR-03** | The system **shall determine** the minimum number of drivers needed to complete every accepted, valid delivery request without overlapping assignments. | Project Overview, p. 1; input-rejection policy specified below | Explicit business objective; validity qualifier is a team decision |


**Accuracy note:** The assignment's "every delivery request" objective assumes valid input. If the team-selected validation policy skips invalid rows, reports must clearly state that results cover **accepted records only**, not every row submitted in the original file.

## 3. Delivery Data Requirements

### 3.1 Minimum Delivery Record

Each delivery request has **at least** the following fields; additional fields are permitted by the wording of the assignment but are not required for this baseline.

| ID | Field / property | Requirement | Status |
| - | - | - | - |
| **DR-01** | `id` | The request shall have an associated identifier. | Explicit |
| **DR-02** | `start_time` | The request shall have an integer start time. | Explicit |
| **DR-03** | `finish_time` | The request shall have an integer finish time. | Explicit |
| **DR-04** | Interval order | A valid request shall satisfy `start_time < finish_time`. | Explicit |
| **DR-05** | ID uniqueness | IDs shall be unique **within an input dataset**; a repeated ID is an invalid record. | Team decision, OQ-01 |
| **DR-06** | Time domain | Both times shall be **nonnegative integer minute offsets**; no application-specific maximum is imposed, apart from the chosen integer type's representable range. | Team decision, OQ-06 |


**ID format:** Values such as `D01` are examples, **not** a requirement that identifiers follow a specific prefix, width, or numbering scheme. Uniqueness is a semantic rule independent of the eventual C++ class/struct choice.

### 3.2 Illustrative Source Data

The assignment supplies the following records as an example. Their layout in the PDF is **not** a prescribed file format, and their time units are **not explicitly defined** there.

| id | start\_time | finish\_time |
| - | -: | -: |
| D01 | 10 | 13 |
| D02 | 2 | 5 |
| D03 | 4 | 7 |
| D04 | 1 | 8 |
| D05 | 8 | 11 |
| D06 | 11 | 14 |
| D07 | 13 | 16 |


### 3.3 Selected Time Interpretation

For this project, time fields will be interpreted as **integer minute offsets from an unspecified origin**, rather than wall-clock timestamps. For example, `(60, 120)` represents an interval lasting 60 minutes.

- The origin is not tied to midnight or a calendar date.

- Negative values are rejected under the team's input policy.

- The system does not apply a 24-hour or 1,440-minute rollover or a daily cutoff.

- A value may exceed 1,440; the model treats it as a later minute offset, not automatically the next day's clock time.

- No time-zone, clock-display, or date-conversion features are included.

**Distinction:** Only *integer representation* and *start before finish* are imposed by the overview/data sections. **Minutes**, **nonnegative values**, and **no application-specific maximum** are team choices.

## 4. Scheduling Rules and Data Invariants

| ID | Rule or invariant | Basis | Status |
| - | - | - | - |
| **BR-01** | A driver shall not be scheduled to perform overlapping delivery intervals. | Project Overview, p. 1 | Explicit |
| **BR-02** | If one delivery finishes at `t` and another starts at `t`, the same driver may perform both. | Delivery Data, p. 2 | Explicit |
| **INV-01** | Each delivery's identifier, start time, and finish time shall remain associated as records are reordered or evaluated. | Data integrity | Team invariant |
| **INV-02** | Scheduling calculations shall not modify the original fields of an accepted delivery request. | Requirements review | Team invariant |


Input file ordering is **unrestricted**. Any ordering or sorting required by a particular scheduling operation is that operation's responsibility; input records are not required to arrive sorted.

## 5. Input Contract and Error Handling

The requirements in this section are **team-selected conventions**, not additional assignment mandates. They keep the project testable and errors traceable without creating a data-repair subsystem.

### 5.1 File Format

| ID | Requirement | Status |
| - | - | - |
| **IR-01** | Delivery input shall use a **headerless, comma-delimited** text file (`.csv`), with exactly three fields per record in this order: `id,start_time,finish_time`. | Agreed, OQ-03 |
| **IR-02** | Record order may be arbitrary; no sorted-input prerequisite is imposed. | Agreed, OQ-02 |
| **IR-03** | Incidental whitespace surrounding field values may be trimmed. Field contents shall not be inferred or reconstructed when missing or malformed. | Lightweight parsing convention |


Illustrative file content (not a required test case):

```
D01,10,13
D02,2,5
D03,4,7
```

No header line is expected. A record's ID and both integer fields must remain linked regardless of how the data is later ordered.

### 5.2 Record Processing

| ID | Requirement | Status |
| - | - | - |
| **IR-04** | The system shall validate that each parsed row has the required fields and that its values satisfy agreed data constraints, including ID uniqueness and `0 <= start_time < finish_time`. | Team decision |
| **IR-05** | If a row is invalid, the system shall **skip that row, log the error, and continue** processing other valid rows. It shall not guess missing values or repair data. | Agreed, OQ-04 |
| **IR-06** | A successfully opened **empty file** is a valid dataset, not an input error. The system shall report that no deliveries need scheduling and shall represent zero deliveries in its results. | Agreed, OQ-05 |
| **IR-07** | Failure to open or read the input file shall be reported as an error; it shall not be treated as a valid empty dataset. | Baseline failure-handling decision |


**Interpretation:** Rejection due to malformed input is **not** the same as an algorithm choosing not to schedule an otherwise valid delivery. Future task requirements must keep those concepts separate.

### 5.3 Processing Report and Diagnostics

| ID | Requirement | Status |
| - | - | - |
| **IR-08** | After input processing, report counts of total nonblank record rows encountered, valid records accepted, and records skipped. | Agreed, OQ-04 |
| **IR-09** | For each invalid record, write an audit-friendly log entry including source filename, line number, reason, and delivery ID if readable; include the character position when reasonably identifiable. | Agreed, OQ-04 |
| **IR-10** | When records are skipped, the user-facing summary shall direct the reader to the diagnostic log (for example, `See errors.log for details.`) and identify that scheduling results use accepted records only. | Agreed, OQ-04 |


`errors.log` is a **working filename**, not a mandated logging framework or directory structure. Exact error-message wording and command-line UX remain design details.

## 6. Engineering Decisions

| ID | Decision | Rationale | Current state |
| - | - | - | - |
| **ED-01** | Use **C++17**. | Supports an algorithms-focused project with suitable standard-library facilities. | Preferred language |
| **ED-02** | Adopt headerless CSV for normal delivery records and reproducible test fixtures. | Simple file input consistent with the assignment's freedom to choose a format. | Agreed |
| **ED-03** | Treat accepted delivery records as read-only during scheduling. | Protects identifiers and time intervals from accidental mutation. | Principle agreed |
| **ED-04** | Keep validation bounded: skip and log bad records; do not implement automatic repair. | Auditability without scope expansion. | Agreed |
| **ED-05** | Maintain representative datasets and test cases, including empty and unordered inputs. | Verifies observable system behavior. | Agreed in principle |


**Not yet decided:** `class` versus `struct`, getters, setters, parsing library, log implementation, CLI design, module boundaries, algorithm selection, and test harness. These are **design decisions**, not prerequisites for this SRS baseline.

## 7. Resolution of Open Questions

All six high-level questions from v0.1 have been resolved through requirements discussion. These are documented **team decisions** unless otherwise identified.

| ID | Question | Resolution | Status |
| - | - | - | - |
| **OQ-01** | Must delivery IDs be unique? | **Yes**, within a dataset; duplicates are invalid and logged. | Resolved |
| **OQ-02** | Must delivery records arrive sorted? | **No**; arbitrary input order is valid. Per-operation ordering is deferred. | Resolved |
| **OQ-03** | What input-file convention applies? | **Headerless CSV**, comma-separated `id,start_time,finish_time`; trim incidental whitespace. | Resolved |
| **OQ-04** | What happens to malformed rows? | Skip and log with source location and reason; process valid rows; report counts and log location. | Resolved |
| **OQ-05** | Is empty input valid? | **Yes**; report no deliveries and zero-sized results. | Resolved |
| **OQ-06** | How are times represented and bounded? | Nonnegative integer **minute offsets**, unspecified origin, no application-specific upper limit or 24-hour rollover. | Resolved |


**No unresolved project-overview/data questions remain at this review stage.** New questions may emerge when examining the task sections and will be tracked in the appropriate task SRS or elevated here if they affect the whole project.

## 8. Deferred Scope and Next Review

The following are deliberately **not** defined in this project-wide SRS:

- Detailed specifications for **Task A, Task B, Task C, and Task D** (each will receive a separate task-level SRS).

- Task-specific input differences, outputs, algorithms, sorting criteria, correctness arguments, runtimes, and acceptance tests.

- Software architecture, data structures, module and class organization, and code.

- Detailed build/run instructions and submission packaging; these will be addressed when reviewing the assignment's submission section.

**Next planned discussion:** Review **Tasks A–D broadly as a group**, to map their responsibilities, relationships, and possible shared concerns. **Do not begin detailed Task A analysis or implementation yet.** After agreeing on the high-level scope, create separate linked SRS documents for the individual tasks.

## 9. Source and Traceability

**Primary source:** `CS413-PA-partI-2026Fall.pdf`, *Pioneer Delivery Company's Algorithm Portfolio — Part I: Greedy Algorithms*, pp. 1–2, specifically **Project Overview** and **Delivery Data**.

Assignment-derived requirements are marked **Explicit**. Every rule not directly required by those sections is labeled **Team decision** or **Team invariant**. The details of Tasks A–D and the submission instructions appear later in the assignment and should be examined in their own requirements review, not inferred from this baseline.

