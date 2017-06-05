# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).


## [Unreleased]
### Changed
- Bump LWJGL to v3.1.2 and JOML to v1.9.3 (e40246c, d71b37f)

### Removed
- Finalizers (d71b37f)


## [2.1.1] - 2016-12-01
### Added
- FlippingFunc in `ImageCreator.java` (ffd6fc3)
- BYTES field for class size (9dbd36f, 2991a98)

### Changed
- Configure exec:java to run TutorialChooser in `pom.xml` (bc25bc4) (thanks to @Spasi)

### Fixed
- Resource loading (d96b706) (thanks to @Spasi)

### Removed
- Custom exceptions (92ad15a, 11ffffc, f7a3a5b)


## [2.1.0] - 2016-11-04
### Added
- Links to original source files (6740859d4a97a5b5cfae5c3b44eb0edf20d1809d)
- `CHANGELOG.md`
- Maven `pom.xml` and common `.gitignore` (thanks to @httpdigest)

### Changed
- Switched to MIT license
- Replaced usages of ImageIO with STBImage (c3723ffb238476cc7702527ce4fc26904f723a5c)

### Fixed
- Updated link to LM3DGP in `README.md` (e67ddda9f06dbb3bc9c62cec44b83490e40f63cb)
- Initialized key callbacks before `init()` in `Tutorial.java` (e0553fad7d85af711fe55ae2bb077d4d96f7bb48)
- Clamped dot product in `Interpolation.java`(9f9dde49faf4f6732761a82ecfeea9fbbc9c438f)

### Removed
- `glm` package (0f6630e8d52a10c1e68bb624802fc9c6269db1ee, ee480cb66e83ea3f4c27548caa39643a422f14aa)


## [2.0.0] - 2016-09-15
### Added
- `commons` package to collect a few classes used in several tutorials

### Changed
- Ported to LWJGL 3
- Replaced math classes with JOML
- Replaced `Framework.degToRad()` with the same function in the Java `Math` class (b9520d3b75e533badec53b9604f2131ef0c4039a)
- Tested on LWJGL 3.0.0 build 90 and JOML 1.8.3

### Fixed
- Colors name in `BaseVertexOverlap.java` (2e83f9363a86a54586dd14cdc55efe2498565823)
- VBO not unbinded in `Tut1.java` (3c330a1950fcdfbfc9344f2157315c9dc9f26ce8)
- Missing call to `init()` in `Tutorial.java` (4f906d12ce413ebd0f8bd49fd1f50c52a0a05259)

### Removed
- `MatrixStack.java` and almost all the math classes, replaced by their JOML counterparts


[Unreleased]: https://github.com/integeruser/jgltut/compare/v2.1.1...HEAD
[2.1.1]: https://github.com/integeruser/jgltut/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/integeruser/jgltut/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/integeruser/jgltut/compare/v1.0.2...v2.0.0
