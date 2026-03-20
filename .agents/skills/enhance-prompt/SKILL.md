---
name: enhance-prompt
description: Read prompt.md, resolve all file/path references, and rewrite it as a clear, structured, Stitch-optimized prompt for UI generation
allowed-tools:
  - "Read"
  - "Write"
  - "Glob"
  - "Grep"
---

# Enhance Prompt Workflow

Transform raw natural language in `prompt.md` into a precise, structured, Stitch-optimized prompt that produces better UI generation results. Read the prompt, resolve every file/path reference, and use actual code and design context to fill in what the original prompt assumed but didn't state.

**Golden rule:** If a colleague with no context on the task would be confused by the prompt, Stitch will be too.

## Prerequisites

Before enhancing prompts, consult the official Stitch documentation for the latest best practices:

- **Stitch Effective Prompting Guide**: https://stitch.withgoogle.com/docs/learn/prompting/

## Steps

### 1. Read the raw prompt

Read the full contents of `prompt.md` at the root of the workspace.

### 2. Extract and investigate all references

<investigate_before_answering>
Never speculate about code you have not opened. Scan the prompt for anything that looks like a path, file, or design reference:

- Explicit paths: `apps/foo/src/bar.tsx`, `components/LoginScreen.swift`
- File names mentioned by name: `HomeView.swift`, `LoginActivity.kt`
- Folder names that imply a module or feature: `features/auth`, `screens/dashboard`
- Design references: `DESIGN.md`, color systems, component libraries
- Any `@[...]` references if present

For each reference found, **read the file before proceeding**. If a directory is referenced, list its contents and read the most relevant files (entry point, index, main component, etc.). Never make claims about code without investigating first — give grounded and hallucination-free answers.
</investigate_before_answering>

### 3. Check for DESIGN.md

Look for a `DESIGN.md` file in the current project:

**If DESIGN.md exists:**
1. Read the file to extract the design system block
2. Include the color palette, typography, and component styles
3. Format as a "DESIGN SYSTEM (REQUIRED)" section in the output

**If DESIGN.md does not exist:**
1. Add a tip at the end suggesting the user create one for consistency

### 4. Analyze the gap between the prompt and available context

With the actual code and design context in hand, identify what the prompt left implicit or ambiguous:

- What platform is this for? (iOS, Android, Web)
- What does the existing implementation look like? (component structure, styles, patterns)
- What is the desired output the user is asking for?
- Are there naming conventions, folder structures, or patterns in the codebase?
- What should the agent NOT touch or change?
- Are there dependencies, imports, or shared utilities the new code must use?

### 5. Apply Enhancements

Transform the input using these techniques:

#### A. Add UI/UX Keywords

Replace vague terms with specific component names (see `references/KEYWORDS.md` for full reference):

| Vague | Enhanced |
|-------|----------|
| "menu at the top" | "navigation bar with logo and menu items" |
| "button" | "primary call-to-action button" |
| "list of items" | "card grid layout" or "vertical list with thumbnails" |
| "form" | "form with labeled input fields and submit button" |
| "picture area" | "hero section with full-width image" |

#### B. Amplify the Vibe

Add descriptive adjectives to set the mood:

| Basic | Enhanced |
|-------|----------|
| "modern" | "clean, minimal, with generous whitespace" |
| "professional" | "sophisticated, trustworthy, with subtle shadows" |
| "fun" | "vibrant, playful, with rounded corners and bold colors" |
| "dark mode" | "dark theme with high-contrast accents on deep backgrounds" |

#### C. Structure the Page

Organize content into numbered sections when applicable.

#### D. Format Colors Properly

When colors are mentioned, format them as:
```
Descriptive Name (#hexcode) for functional role
```

### 6. Rewrite prompt.md as a structured instruction

Produce a new, complete version of the prompt using the following XML-structured format. **Write it back to `prompt.md`, replacing the old content entirely.**

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

Describe the project structure and relevant background. Mention what already exists, what the user has been working on, and what problem they are solving. Use specifics from the code you read — component names, file paths, prop interfaces, style patterns, etc.

Provide motivation: explain WHY this task matters or WHY certain constraints exist.

If a DESIGN.md exists, include the design system block here.

If referencing multiple files, structure them with nested XML tags:]

<documents>
<document index="1">
<source>path/to/file</source>
<document_content>
[Relevant code snippet or summary]
</document_content>
</document>
</documents>
</context>

<instructions>
## Task

[One-line description of the page/component purpose and vibe]

**DESIGN SYSTEM (REQUIRED):**
- Platform: [iOS/Android/Web], [orientation/size class]
- Theme: [Light/Dark], [style descriptors]
- Background: [Color description] (#hex)
- Primary Accent: [Color description] (#hex) for [role]
- Text Primary: [Color description] (#hex)
- [Additional design tokens...]

**Page Structure:**
1. **[Section]:** [Description with specific component keywords]
2. **[Section]:** [Description with specific component keywords]
...

[State exactly what the agent must do. Use numbered steps when order matters. Be explicit about:
1. What files to create (with exact target paths)
2. What files to modify (with exact paths and what changes to make)
3. What the new component must expose (exports, props, interface)]
</instructions>

<examples>
## Design / Pattern to Follow

[Pull from existing code or DESIGN.md the user referenced as a "good example".]

<example>
<description>Component structure from existing implementation</description>
<code>
[Actual code snippet showing the pattern to follow]
</code>
</example>
</examples>

<constraints>
## Constraints

<over_engineering_prevention>
Keep solutions minimal and focused on the current task:

**Scope:** Only make changes that are directly requested or clearly necessary.
**Abstractions:** Do not create helpers or utilities for one-time operations.
**File creation:** Do not create extra files unless strictly necessary.
</over_engineering_prevention>

**Scope boundaries:**
- Only modify files explicitly listed in the Task section
- Follow existing patterns exactly — do not introduce new abstractions
- Use existing dependencies — add new ones only if strictly necessary

**Context:** This is a [new page / targeted edit]. [Make only this change while preserving all existing elements / Build this as a new screen.]
</constraints>

<expected_output>
## Expected Output

**Files created:**
- `path/to/new/file` — [purpose]

**Files modified:**
- `path/to/existing/file` — [what changed]

**Verification checklist:**
- [ ] All paths are real, verified paths from the workspace
- [ ] New code follows the exact pattern from the example
- [ ] No unrelated files were modified
- [ ] Existing functionality is preserved
- [ ] No unnecessary files, helpers, or abstractions were created
</expected_output>
```

</output_template>

### 7. Verify the rewritten prompt

Before finishing, re-read the rewritten `prompt.md` and confirm:

<verification_checklist>

- Every path mentioned is a real, verified path from the workspace
- The task is broken into discrete, numbered steps
- No step requires the agent to guess or infer something not stated
- Context and motivation are provided (WHY, not just WHAT)
- Examples are provided for any non-trivial pattern
- Constraints explicitly prevent over-engineering
- The expected output includes a verification checklist
- The design system section is populated (from DESIGN.md or inferred)
</verification_checklist>

If anything is still vague or relies on an assumption, resolve it using the code you already read and update `prompt.md` accordingly.

## Tips for Best Results

1. **Be specific early** — Vague inputs need more enhancement
2. **Match the user's intent** — Don't over-design if they want simple
3. **Keep it structured** — Numbered sections help Stitch understand hierarchy
4. **Include the design system** — Consistency is key for multi-page projects
5. **One change at a time for edits** — Don't bundle unrelated changes
6. **Investigate before writing** — Read referenced files to ground the prompt in reality
