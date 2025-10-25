# Contributing to Kyo Playground

Thank you for your interest in contributing to the Kyo Playground! This document provides guidelines for contributing examples and improvements.

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a new branch for your feature/example
4. Make your changes
5. Test your changes thoroughly
6. Submit a pull request

## Adding New Examples

When adding new examples to demonstrate Kyo features:

1. **Choose a Clear Focus**: Each example should demonstrate a specific Kyo effect or pattern
2. **Place in Appropriate Package**: 
   - Core examples go in `com.example`
   - Choice-related examples go in `com.example.choice`
   - Create new subpackages for other effect categories as needed

3. **Follow the Example Structure**:
   ```scala
   package com.example
   
   import kyo.*
   
   /** Brief description of what this example demonstrates.
     * 
     * Key concepts:
     * - Point 1
     * - Point 2
     */
   object ExampleName extends KyoApp:
     // Implementation
     run {
       // Your effect composition here
     }
   end ExampleName
   ```

4. **Add Documentation**:
   - Include a scaladoc comment explaining the example's purpose
   - Add inline comments for complex Kyo patterns
   - Update the README.md with your new example

## Code Style

### Formatting
- Use Scalafmt for consistent formatting: `sbt scalafmtAll`
- Configuration is defined in `.scalafmt.conf`

### Scala 3 Best Practices
- Use new Scala 3 syntax (indentation-based, `end` markers, etc.)
- Prefer `given` instances over implicit parameters
- Use opaque types for type-safe domain modeling
- Leverage union types and match types where appropriate

### Kyo Best Practices
- Keep effect signatures explicit and minimal
- Use for-comprehensions for sequential effect composition
- Prefer direct style over callback-based APIs
- Handle errors appropriately with `Abort` effect
- Document which effects are demonstrated in comments

## Testing

- Add tests for new functionality in `src/test/scala`
- Use munit for testing (project dependency)
- Ensure all tests pass: `sbt test`
- Verify compilation: `sbt compile`

## Documentation

- Keep README.md up to date with new examples
- Add inline documentation for complex algorithms
- Include example output in comments where helpful
- Document any non-obvious Kyo patterns used

## Commit Messages

Write clear, concise commit messages:
- Use present tense ("Add example" not "Added example")
- Reference issues when applicable
- Keep the first line under 72 characters
- Provide additional context in the body if needed

Example:
```
Add Retry effect example

Demonstrates automatic retry logic with exponential backoff
using Kyo's Retry effect. Includes error simulation and
backoff strategy configuration.
```

## Pull Request Process

1. Ensure your code compiles and all tests pass
2. Run scalafmt before submitting
3. Update documentation as needed
4. Write a clear PR description explaining your changes
5. Link to any related issues
6. Wait for review and address any feedback

## Questions?

If you have questions about Kyo or need help with your contribution:
- Check the [Kyo documentation](https://getkyo.io)
- Review existing examples for patterns
- Open an issue for discussion

## Code of Conduct

- Be respectful and constructive in all interactions
- Welcome newcomers and help them learn
- Focus on what's best for the community
- Show empathy towards other contributors
