---
name: enhance-prompt
description: Read prompt.md, resolve all file/path references, and rewrite it as a clear, structured, agent-ready instruction set
allowed-tools:
  - "Read"
  - "Write"
  - "Glob"
  - "Grep"
---
# Improve Prompt Workflow

Transform raw natural language in `prompt.md` into a precise, unambiguous instruction set that any agent can follow without guessing. Read the prompt, resolve every file/path reference, and use actual code to fill in context the original prompt assumed but didn't state.

**Golden rule:** Show your prompt to a colleague with minimal context on the task and ask them to follow it. If they'd be confused, the agent will be too.

## Steps

### 1. Read the raw prompt

Read the full contents of `prompt.md` at the root of the workspace.

### 2. Extract and investigate all references

<investigate_before_answering>
Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Scan the prompt for anything that looks like a path or file reference:

- Explicit paths: `shared/src/commonMain/kotlin/mx/atriz/Greeting.kt`, `android/fraccio/src/androidMain/kotlin/mx/atriz/fraccio/App.kt`
- File names mentioned by name: `Platform.kt`, `Platform.android.kt`, `ContentView.swift`
- Folder names that imply a module or source set: `shared/src/commonMain`, `shared/src/androidMain`, `shared/src/iosMain`
- Gradle build files: `shared/build.gradle.kts`, `gradle/libs.versions.toml`
- Any `@[...]` references if present

For each reference found, **read the file before proceeding**. If a directory is referenced, list its contents and read the most relevant files (entry point, expect/actual declarations, Composable functions, SwiftUI views, etc.). Never make claims about code without investigating first — give grounded and hallucination-free answers.
</investigate_before_answering>

### 3. Analyze the gap between the prompt and the code

With the actual code in hand, identify what the prompt left implicit or ambiguous:

- What does the existing implementation actually look like? (Composable structure, view modifiers, expect/actual declarations, Koin modules)
- Which source set does the code belong to? (`commonMain` for shared logic, `androidMain`/`iosMain` for platform-specific)
- What Gradle module boundaries exist? (`shared`, `android/*`, `ios/*`)
- What DI patterns are used? (Koin modules and bindings)
- What networking layer exists? (Ktor client configuration)
- What database patterns are used? (SQLDelight queries and drivers)
- What is the desired output the user is asking for? Can you infer it from the code they pointed to as the "good" example?
- Are there naming conventions, folder structures, or patterns in the codebase the agent must follow?
- What should the agent NOT touch or change?
- Are there any dependencies, imports, or shared utilities the new code must use or export from?

### 4. Rewrite the prompt as a structured agent instruction

Produce a new, complete version of the prompt using the following XML-structured format. Write it to `prompt.md`, replacing the old content entirely.

**Key principles for the rewritten prompt:**

- **Longform data at the top** — Place documents and context above instructions (improves response quality by up to 30%)
- **Numbered steps** — Use sequential numbered lists when order or completeness matters
- **Concrete examples** — Examples are the most reliable way to steer output format, tone, and structure
- **XML structure** — Use consistent, descriptive tag names; nest tags when content has natural hierarchy

<output_template>

```markdown
<context>
## Context

[Place longform context at the TOP of the prompt.

Describe the project structure and relevant background. Mention what already exists, what the user has been working on, and what problem they are solving. Use specifics from the code you read — Composable names, source set locations, expect/actual interfaces, Gradle module names, Koin bindings, etc.

Provide motivation: explain WHY this task matters or WHY certain constraints exist — this helps the agent understand goals and deliver more targeted responses.

If referencing multiple files, structure them with nested XML tags:]

<documents>
<document index="1">
<source>shared/src/commonMain/kotlin/mx/atriz/Platform.kt</source>
<document_content>
[Relevant code snippet or summary]
</document_content>
</document>
<document index="2">
<source>android/fraccio/src/androidMain/kotlin/mx/atriz/fraccio/App.kt</source>
<document_content>
[Relevant code snippet or summary]
</document_content>
</document>
<document index="3">
<source>ios/fraccio/ContentView.swift</source>
<document_content>
[Relevant code snippet or summary]
</document_content>
</document>
</documents>
</context>

<instructions>
## Task

[State exactly what the agent must do. Use numbered steps when order matters — this ensures completeness. Be explicit about:

1. What files to create (with exact target paths and source set placement)
2. What files to modify (with exact paths and what changes to make)
3. Which source set each file belongs to (`commonMain`, `androidMain`, `iosMain`)
4. What expect/actual declarations are needed and where each part lives
5. What Gradle dependencies to add (module and `libs.versions.toml` alias)
6. What Koin bindings or module registrations are required

If you want thorough or "above and beyond" behavior, explicitly request it rather than relying on inference.]
</instructions>

<examples>
## Design / Pattern to Follow

[Provide concrete examples — they are the most reliable way to steer output format, tone, and structure. Pull from the "good example" file the user referenced.

Make examples:

- **Relevant**: Mirror the actual use case closely
- **Diverse**: Cover edge cases if applicable
- **Structured**: Wrap in `<example>` tags so the agent distinguishes them from instructions]

<example>
<description>Compose Composable with Koin DI</description>
<code>
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
        ) {
            Text(state.title)
        }
    }
}
</code>
</example>

<example>
<description>SwiftUI view consuming Shared framework</description>
<code>
import SwiftUI
import Shared

struct FeatureView: View {
    @StateObject private var viewModel = FeatureViewModel()

    var body: some View {
        VStack {
            Text(viewModel.title)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
}
</code>
</example>

<example>
<description>Expect/actual platform pattern</description>
<code>
// commonMain — shared/src/commonMain/kotlin/mx/atriz/Platform.kt
interface Platform {
    val name: String
}
expect fun getPlatform(): Platform

// androidMain — shared/src/androidMain/kotlin/mx/atriz/Platform.android.kt
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}
actual fun getPlatform(): Platform = AndroidPlatform()

// iosMain — shared/src/iosMain/kotlin/mx/atriz/Platform.ios.kt
class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}
actual fun getPlatform(): Platform = IOSPlatform()
</code>
</example>

[Include:

- Composable structure and modifier chains
- SwiftUI view body patterns and Shared framework imports
- Expect/actual declaration pairs with source set locations
- Koin module registration and injection patterns
- Ktor client setup and repository patterns
- SQLDelight query and driver patterns where relevant]
</examples>

<constraints>
## Constraints

<over_engineering_prevention>
Keep solutions minimal and focused on the current task:

**Scope:** Only make changes that are directly requested or clearly necessary. A bug fix doesn't need surrounding code cleaned up. A simple feature doesn't need extra configurability.

**Documentation:** Do not add docstrings, comments, or type annotations to code you didn't change. Only add comments where the logic isn't self-evident.

**Abstractions:** Do not create helpers, utilities, or abstractions for one-time operations. Do not design for hypothetical future requirements. The right amount of complexity is the minimum needed for the current task.

**File creation:** Do not create extra files, helper scripts, or unnecessary abstractions. If temporary files are needed for iteration, clean them up at the end.
</over_engineering_prevention>

**Scope boundaries:**

- Only modify files explicitly listed in the Task section
- Follow existing patterns exactly — do not introduce new abstractions
- Use existing dependencies — add new ones only if strictly necessary
- Maintain all existing exports and public API
- Place shared logic in `commonMain`; platform-specific code in `androidMain` or `iosMain`
- Add Gradle dependencies to the correct source set block in `build.gradle.kts`
- Add new version catalog entries to `gradle/libs.versions.toml`
- Register new classes in the appropriate Koin module

**Quality standards:**

- Write general-purpose solutions, not solutions that only work for specific test inputs
- Implement actual logic that solves the problem generally
- If tests exist, they verify correctness — do not hard-code values to pass them
</constraints>

<expected_output>

## Expected Output

[Define clear success criteria. Describe what "done" looks like:]

**Files created:**

- `shared/src/commonMain/kotlin/mx/atriz/feature/FeatureRepository.kt` — [purpose]
- `shared/src/androidMain/kotlin/mx/atriz/feature/FeatureDriver.android.kt` — [platform actual]
- `shared/src/iosMain/kotlin/mx/atriz/feature/FeatureDriver.ios.kt` — [platform actual]
- `android/fraccio/src/androidMain/kotlin/mx/atriz/fraccio/feature/FeatureScreen.kt` — [Compose UI]
- `ios/fraccio/FeatureView.swift` — [SwiftUI UI]

**Files modified:**

- `shared/build.gradle.kts` — [dependency added to commonMain/androidMain/iosMain]
- `gradle/libs.versions.toml` — [new version catalog entry]

**Verification checklist:**

- [ ] All paths are real, verified paths from the workspace
- [ ] New code follows the exact pattern from the example
- [ ] No unrelated files were modified
- [ ] Existing functionality is preserved
- [ ] No unnecessary files, helpers, or abstractions were created
- [ ] Source set placement is correct (`commonMain` vs `androidMain`/`iosMain`)
- [ ] Every `expect` declaration has matching `actual` implementations for both Android and iOS
- [ ] Gradle dependencies are in the correct source set block
- [ ] Koin bindings are registered for any new injectable classes
</expected_output>
```

</output_template>

### 5. Verify the rewritten prompt

Before finishing, re-read the rewritten `prompt.md` and confirm:

<verification_checklist>

- Every path mentioned is a real, verified path from the workspace
- The task is broken into discrete, numbered steps
- No step requires the agent to guess or infer something not stated
- Context and motivation are provided (WHY, not just WHAT)
- Examples are provided for any non-trivial pattern
- Constraints explicitly prevent over-engineering
- The expected output includes a verification checklist
- Source set placement is specified for every new file
- Every `expect` declaration has corresponding `actual` implementations listed
- Gradle dependency placement (which source set block) is explicit
</verification_checklist>

If anything is still vague or relies on an assumption the agent would have to make, resolve it using the code you already read and update the prompt accordingly.

## Best Practices Applied

This workflow incorporates Claude's prompting best practices:

### General Principles

1. **Be clear and direct** — Explicit instructions with numbered steps; if you want "above and beyond" behavior, explicitly request it
2. **Add context at the top** — Longform data placed before instructions (up to 30% quality improvement)
3. **Provide motivation** — Explain WHY constraints exist to help the agent understand goals
4. **Use examples effectively** — Concrete code examples wrapped in `<example>` tags; make them relevant, diverse, and structured
5. **Structure with XML tags** — `<context>`, `<instructions>`, `<examples>`, `<constraints>` for unambiguous parsing; use consistent tag names

### Agentic Coding

6. **Investigate before answering** — Never speculate about code not read; give grounded, hallucination-free answers
7. **Prevent over-engineering** — Explicit constraints against unnecessary complexity, extra files, and hypothetical future requirements
8. **General-purpose solutions** — Avoid hard-coding values or solutions that only work for specific test inputs
9. **Minimize file creation** — Do not create helper scripts or extra files unless strictly necessary; clean up temporary files
10. **Define success criteria** — Clear verification checklist for completion
