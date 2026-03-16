---
description: Read prompt.md, resolve all file/path references, and rewrite it as a clear, structured, agent-ready instruction set
---

# Improve Prompt Workflow

Your job is to transform the raw natural language in `prompt.md` into a precise, unambiguous instruction set that any agent can follow without guessing. You do this by reading the prompt, resolving every file or path reference it contains, and using the actual code to fill in context the original prompt assumed but didn't state.

## Project context

This is a **Kotlin Multiplatform (KMP)** mobile project housing two apps — **Fraccio** (residential management) and **Atriz Admin** (tenant/product management). The project is in early stage.

**Key structure:**
```
atriz-mobile/
├── shared/                          # KMP shared module
│   ├── src/
│   │   ├── commonMain/              # Shared Kotlin code
│   │   │   ├── domain/              # Business logic, use cases
│   │   │   ├── data/                # Repositories, data sources
│   │   │   ├── network/             # API clients, DTOs
│   │   │   └── util/                # Shared utilities
│   │   ├── androidMain/             # Android-specific implementations
│   │   └── iosMain/                 # iOS-specific implementations
│   └── build.gradle.kts
├── android/                         # Android application (Jetpack Compose)
│   ├── fraccio/                     # Fraccio Android app module
│   └── atriz/                       # Atriz Admin Android app module
├── ios/                             # iOS application (SwiftUI)
│   ├── fraccio/                     # Fraccio iOS target
│   └── atriz/                       # Atriz Admin iOS target
├── docs/spec.md                     # Full API spec and data models
├── build.gradle.kts
└── settings.gradle.kts
```

**Tech stack:** Kotlin, Ktor (networking), Koin (DI), SQLDelight (local DB), kotlinx-serialization, Jetpack Compose (Android UI), SwiftUI (iOS UI).

**Architecture patterns:** Repository pattern, Store/StateFlow for state management, platform `expect`/`actual` for native implementations (token storage, DB drivers, HTTP engines).

**Reference spec:** `docs/spec.md` contains all API endpoints, data models, and feature specifications. Always consult it when the prompt references a feature, endpoint, or data model.

## Steps

### 1. Read the raw prompt

Read the full contents of `prompt.md` at the root of the workspace.

### 2. Extract all file and path references

Scan the prompt for anything that looks like a path or file reference. This includes:

- Explicit paths: `shared/src/commonMain/domain/auth/`, `android/fraccio/src/...`
- File names mentioned by name: `AuthRepository.kt`, `HouseStore.kt`, `SyncResponse.kt`
- Module or feature references: `shared/domain/auth`, `android/fraccio`, `ios/atriz`
- Spec references: mentions of API endpoints, data models, or feature sections from `docs/spec.md`
- Any `@[...]` references if present

For each reference found, locate the actual file or directory in the workspace and read its contents. If a directory is referenced, list its contents and read the most relevant files (entry point, module definition, main class, etc.). If the prompt references a feature or endpoint, cross-reference with `docs/spec.md` to get the full data model and API contract.

### 3. Analyze the gap between the prompt and the code

With the actual code in hand, identify what the prompt left implicit or ambiguous:

- What does the existing implementation actually look like? (class hierarchy, interfaces, data classes, platform `expect`/`actual` split)
- What is the desired output the user is asking for? Can you infer it from the code they pointed to as the "good" example?
- Which layer does the work belong in? (`shared/commonMain` for business logic, `androidMain`/`iosMain` for platform code, `android/`/`ios/` for UI)
- Are there naming conventions, package structures, or patterns in the codebase the agent must follow?
- What should the agent NOT touch or change?
- Are there dependencies, Koin modules, or shared interfaces the new code must implement or register with?
- Does `docs/spec.md` contain relevant data models or endpoints the agent needs?

### 4. Rewrite the prompt as a structured agent instruction

Produce a new, complete version of the prompt using the following structure. Write it to `prompt.md`, replacing the old content entirely.

```
## Context

[Describe the project structure and relevant background. Mention what already exists, what the user has been working on, and what problem they are solving. Use specifics from the code you read — class names, file paths, interfaces, data classes, Koin modules, etc. Reference the relevant section of docs/spec.md if applicable.]

## Task

[State exactly what the agent must do. Break it into numbered steps if the task involves multiple actions. Be explicit about:
- What files to create (with exact target paths and package names)
- What files to modify (with exact paths and what changes to make)
- Which KMP source set the code belongs in (commonMain, androidMain, iosMain, or platform app module)
- What interfaces to implement, what Koin modules to register in
- What data models or DTOs to define (reference docs/spec.md for the exact shape)]

## Design / Pattern to Follow

[Describe the exact implementation pattern the agent must replicate. Pull this from the "good example" file the user referenced. Include:
- Class/interface structure (Repository, Store, UseCase, etc.)
- Data class definitions with serialization annotations
- Platform expect/actual split if applicable
- Koin module registration pattern
- UI pattern (Compose @Composable structure or SwiftUI View, depending on platform)]

## Constraints

[List explicit "do NOT" rules. For example:
- Do not change existing files beyond what is specified
- Do not invent new patterns — follow what already exists in the codebase
- Do not add new dependencies unless strictly necessary
- Keep shared logic in commonMain, platform-specific code in androidMain/iosMain
- Follow the data models defined in docs/spec.md
- Register new dependencies in the existing Koin modules]

## Expected Output

[Describe what "done" looks like. Which files will exist that didn't before? What will be modified? What should the agent verify before considering the task complete? E.g.: "The project compiles on both platforms", "The new repository is accessible via Koin injection", etc.]
```

### 5. Verify the rewritten prompt

Before finishing, re-read the rewritten `prompt.md` and confirm:

- Every path mentioned is a real, verified path from the workspace
- The task is broken into discrete, executable steps
- No step requires the agent to guess or infer something not stated
- The correct KMP source set is specified for each file
- Data models match `docs/spec.md`
- The constraints are explicit
- The expected output is unambiguous

If anything is still vague or relies on an assumption the agent would have to make, resolve it using the code you already read and update the prompt accordingly.
