# Learning Modern 3D Graphics Programming with LWJGL 3 and JOML
This project is a port of *[Learning Modern 3D Graphics Programming](https://paroj.github.io/gltut/)* tutorials to Java using [LWJGL](https://www.lwjgl.org/) and [JOML](http://joml-ci.github.io/JOML/). The original project, named `gltut`, can be found [here](https://github.com/paroj/gltut). Since it is needed by the tutorials, this repository also includes a partial port of the *[Unofficial OpenGL SDK](https://glsdk.sourceforge.net/docs/html/index.html)*, named `glsdk`, which contains a DDS texture loader and other useful stuff.

To suggest a feature, report bugs or general discussion use the [issue tracker](https://github.com/integeruser/jgltut/issues). Contributions are welcome! :smile:

## Usage
To set up the project, just import the supplied Maven POM into your favorite IDE and you are done. The code was last tested on:
- Java SE Development Kit 8
- [LWJGL 3.1.2 build 29](https://www.lwjgl.org/download)
- [JOML 1.9.3](https://github.com/JOML-CI/JOML/releases/tag/1.9.3)

To play with the tutorials, run the `main` method of `integeruser.jgltut.TutorialChooser.java` and select the tutorial to run. To quit any tutorial simply press `ESC`.

**Note for Mac OS users**: SWT applications on Mac OS require the JVM option `-XstartOnFirstThread`. Because of this, `TutorialChooser` cannot work on Mac OS.

## Notes
I decided to keep the ported code as similar as possible to the original C++ code, and therefore variables and functions are almost identical to their counterpart in the original projects. The only notable difference is the introduction of the `commons` package to collect some classes used in several parts of the project. I also decided to keep the same directory layout:
```
jglsdk/
|-- glimg/
|-- glutil/

jgltut/
|-- commons/
|-- data/
|-- framework/
|-- tut01/
|------ Tut1.java
|-- tut02/
|------ data/
|------ FragPosition.java
|------ VertexColor.java
|-- ...
|-- ...
|-- ...
|-- tut17/
|------ data/
|------ CubePointLight.java
|------ DoubleProjection.java
|------ ProjectedLight.java
|-- Tutorial.java
|-- TutorialChooser.java
```

## Credits
The LWJGL license can be found [here](http://lwjgl.org/license.php).  
The JOML license can be found [here](https://github.com/JOML-CI/JOML/blob/master/LICENSE).  
Extract from the `gltut` license:
> The following files are copywritten and distributed under the Creative Commons Attribution 3.0 Unported (CC BY 3.0) license, as described in the "./CC BY 3.0 legalcode.txt" file. Attribution for these works is presented here:
>
> Attributed to Etory, of OpenGameArt.org:
> * data/seamless_rock1_small.dds
>
> Attributed to p0ss, of OpenGameArt.org:
> * data/concrete649_small.dds
> * data/dsc_1621_small.dds
> * data/rough645_small.dds
