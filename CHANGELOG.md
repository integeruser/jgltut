# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).


## [Unreleased]
### Added
- Links to original source files (6740859d4a97a5b5cfae5c3b44eb0edf20d1809d)
- `CHANGELOG.md`

### Changed
- Add note on `XstartOnFirstThread` in `README.md` (5ed37f34389f8703a8bfd41810888cb3efde5cbc)
- Switch to MIT license (57f20622f1be542544714066bac816fc98459b34)
- Test on LWJGL 3.1.0 build 40

### Fixed
- Update link to LM3DGP in `README.md` (e67ddda9f06dbb3bc9c62cec44b83490e40f63cb)
- Initialize key callbacks before `init()` in `Tutorial.java` (e0553fad7d85af711fe55ae2bb077d4d96f7bb48)
- Clamp dot product in `Interpolation.java`(9f9dde49faf4f6732761a82ecfeea9fbbc9c438f)

### Removed
- `glm` package (0f6630e8d52a10c1e68bb624802fc9c6269db1ee, ee480cb66e83ea3f4c27548caa39643a422f14aa)


## [2.0.0] - 2016-09-15
### Added
- `commons` package to collect a few classes used in several tutorials

### Changed
- Ported to LWJGL 3
- Replaced math library with to JOML

### Fixed
- Colors name in `BaseVertexOverlap.java` (2e83f9363a86a54586dd14cdc55efe2498565823)
- VBO not unbinded in `Tut1.java` (3c330a1950fcdfbfc9344f2157315c9dc9f26ce8)
- Missing call to `init()` in `Tutorial.java` (4f906d12ce413ebd0f8bd49fd1f50c52a0a05259)

### Removed
- `MatrixStack.java` and most of the math classes, replaced by their JOML counterparts
- `Framework.degToRad()`, replaced by the same function in the Java `Math` class (b9520d3b75e533badec53b9604f2131ef0c4039a)


[Unreleased]: https://github.com/integeruser/jgltut/compare/v2.0.0...develop
[2.0.0]: https://github.com/integeruser/jgltut/compare/v1.0.2...v2.0.0
