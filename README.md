# Learning Modern 3D Graphics Programming with LWJGL 3 and JOML
This project is a port of *[Learning Modern 3D Graphics Programming](http://alfonse.bitbucket.org/oldtut/)* tutorials to Java using [LWJGL](https://www.lwjgl.org/) and [JOML](http://joml-ci.github.io/JOML/), distributed in the hope that it will be useful. The original project, named `gltut`, can be found [here](https://bitbucket.org/alfonse/gltut/wiki/Home). Since it is needed by the tutorials, this repository also includes a partial port of the *[Unofficial OpenGL SDK](https://bitbucket.org/alfonse/unofficial-opengl-sdk/wiki/Home)*, named `glsdk`, which contains a DDS texture loader and other useful stuff.

To try the tutorials without building the source code, you can download the runnable JAR included in the [Releases](https://github.com/integeruser/jgltut/releases) section. Many tutorials print messages to console: run the JAR from the command line with `java -jar jgltut.jar` to view the output (also useful in case of errors).

I can't dedicate much time to this project anymore, but I will keep it updated to work with recent versions of LWJGL and JOML. To suggest a feature, report bugs or general discussion use the [issue tracker](https://github.com/integeruser/jgltut/issues).

Contributions are welcome! :smile:  


## Usage
To compile and run the code, you will need:

- Java SE Development Kit 8
- [LWJGL 3.1.0 build 40](https://www.lwjgl.org/download)
- [JOML 1.9.0](https://github.com/JOML-CI/JOML/releases/tag/1.9.0)

Working with different versions of LJWGL 3 or JOML may require adjustments to the code. If you are stuck with LWJGL 2, check the release [v0.9.1](https://github.com/integeruser/jgltut/releases/tag/v0.9.1) of this repository.

Create a new Java project using your favorite IDE, then:

1. Import the source code of this repository;
2. Download LWJGL from the link above, then add `lwjgl.jar` and the other JARs to the classpath and link the native libraries, as explained in the [official guide](https://www.lwjgl.org/guide);
3. Download `joml-1.9.0.jar` from the link above, then add it to the classpath as in the previous step.

Finally, run the `main` method of the first tutorial `integeruser.jgltut.tut01.Tut1.java` and check the output in the console window. If your graphics card does not meet the minimum requirements (checked using the LWJGL helper `GL.getCapabilities().OpenGL33`), the message `You must have at least OpenGL 3.3 to run this tutorial.` will appear; otherwise, if no other errors show up, you can start playing with the other tutorials by running the `main` method of `integeruser.jgltut.TutorialChooser.java`. To quit any tutorial simply press `ESC`.

**Note for OS X users**: to run SWT applications on OS X it is *necessary* to use the JVM option `XstartOnFirstThread`. Because of this, `TutorialChooser.java` can not work correctly on OS X.


## Notes
I decided to keep the ported code as similar as possible to the original C++ code, despite I would have done some things differently, and variable and function names are almost identical to their counterpart in the original projects. The only notable difference is the introduction of the `commons` package, to collect some classes used in several parts of the project. I also decided to keep the same directory layout:
```
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

jglsdk/
|-- glimg/
|-- glutil/
```
Each tutorial loads the files it needs from the global `integeruser.jgltut.data` folder or from its own `data` folder. The `integeruser.jgltut.framework` package contains utility code needed by multiple tutorials.

At first, the code may appear difficult to read, but after a bit of reading you will realize that it is well-grouped into logical sections. I suggest you to start by skimming through the code with methods and inner classes folded. Don't get lost in the details of the `integeruser.jglsdk` module which contains only utility classes.


## Credits
The LWJGL license can be found [here](http://lwjgl.org/license.php).  
The JOML license can be found [here](https://github.com/JOML-CI/JOML/blob/master/LICENSE).  
Licenses of the projects `gltut` and `glsdk` can be found [here](https://bitbucket.org/alfonse/gltut/raw/3ee6f3dd04a7/License.txt) and
[here](https://bitbucket.org/alfonse/unofficial-opengl-sdk/raw/1893b6e851b9/License.txt).

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
