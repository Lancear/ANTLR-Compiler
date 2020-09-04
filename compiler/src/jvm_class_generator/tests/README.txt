Tests are not up to date!
Locals have to be allocated with code.allocLocal() before using them in this new version of the jvm class generator.
The tests do not do this yet and therefore result into errors.
