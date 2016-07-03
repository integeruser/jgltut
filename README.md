# Learning Modern 3D Graphics Programming with LWJGL 3 and JOML
This project is a port of *[Learning Modern 3D Graphics Programming](https://web.archive.org/web/20150225192611/http://www.arcsynthesis.org/gltut/index.html)* tutorials to Java using [LWJGL](http://www.lwjgl.org) and [JOML](https://github.com/JOML-CI/JOML), distributed in the hope that it will be useful. The original project, named `gltut`, can be found [here](https://bitbucket.org/alfonse/gltut/wiki/Home). Since it is needed by the tutorials, this repository also includes a partial port of the *[Unofficial OpenGL SDK](https://bitbucket.org/alfonse/unofficial-opengl-sdk/wiki/Home)* (`glsdk` for short), which contains a DDS texture loader and other useful stuff.  

To try the tutorials without building the source code, you can download the runnable JAR included in the [Releases](https://github.com/integeruser/jgltut/releases) section. Many tutorials print messages to console: run the JAR from the command line with `java -jar jgltut.jar` to view the output (also useful in case of errors).

To suggest a feature, report bugs or general discussion use the [issue tracker](https://github.com/integeruser/jgltut/issues).

Happy coding! :smile:  
Francesco


## Usage
To compile and run the code, you will need:

- Java SE Development Kit 8
- LWJGL 3 (tested with [3.0.0b build 35](https://www.lwjgl.org/download))
- JOML (tested with [1.8.1](https://github.com/JOML-CI/JOML/releases/tag/1.8.1))

Working with different versions of LJWGL 3 or JOML may require adjustments to the code. If you are stuck with LWJGL 2, you can check the release [v0.9.1](https://github.com/integeruser/jgltut/releases/tag/v0.9.1) of this repository.

Create a new Java project using your favorite IDE, then:

1. Import the source code of this repository;
2. Add `lwjgl.jar` to the classpath and link the LWJGL native libraries, as explained [here](http://www.lwjgl.org/guide);
3. Import the source code of the JOML library.

Finally, run the `main` method of the first tutorial `jgltut.tut01.Tut1` and check the output in the console window. If no error messages appear, then you can start playing with the other tutorials by running the `main` method of `TutorialChooser.java`. To quit any tutorial simply press `ESC`.


## Notes
I decided to keep the ported code as similar as possible to the original C++ code, despite I would have done some things differently; hence, variables and functions are almost identical to their counterparts in the original projects. I also decided to keep the same project layout:
```
jgltut/
|-- data/
|-- framework/
|-- jglsdk/
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
|-- LWJGLWindow.java
|-- TutorialChooser.java
```
Each tutorial loads the files it needs from the global `data` folder or from its own `data` folder. The `framework` package contains utility code needed by multiple tutorials. The `jglsdk` package contains the port of the `glsdk` project. The class `LWJGLWindow.java` is used to create and display a LWJGL window, and `TutorialChooser.java` is a handy program for quickly executing the various tutorials.  
At first, the code may appear difficult to read, but after a bit of reading you will realize that it is well-grouped into logical sections. I suggest you to start by skimming through the code with methods and inner classes folded. Don't get lost in the details of the `jglsdk` module (which is quite big and boring), and remember that the interesting code is the one contained in the tutorials.

If your graphics card does not meet the minimum requirements, running any tutorial will print to the console the message `You must have at least OpenGL 3.3 to run this tutorial.`. The requirements are checked in `LWJGLWindow.java` using the LWJGL function `GL.getCapabilities().OpenGL33`.

I can't dedicate much time to this project anymore, but in the future I will probably:

- keep refactoring code I don't like;
- rework some parts of `jglsdk`;
- rework the `TutorialChooser`.


## Credits
The LWJGL license can be found [here](http://lwjgl.org/license.php).  
Licenses of the projects `gltut` and `glsdk` can be found [here](https://bitbucket.org/alfonse/gltut/raw/3ee6f3dd04a7/License.txt) and
[here](https://bitbucket.org/alfonse/unofficial-opengl-sdk/raw/1893b6e851b9/License.txt).

Extract from the `gltut` license:
```
The following files are copywritten and distributed under the Creative Commons Attribution 3.0 Unported (CC BY 3.0) license, as described in the "./CC BY 3.0 legalcode.txt" file. Attribution for these works is presented here:

Attributed to Etory, of OpenGameArt.org:
* data/seamless_rock1_small.dds

Attributed to p0ss, of OpenGameArt.org:
* data/concrete649_small.dds
* data/dsc_1621_small.dds
* data/rough645_small.dds
```


## License
This project is licensed under the [Attribution 4.0 International license](http://creativecommons.org/licenses/by/4.0/): you can do what you want with my code, but if you want to publish some derived work I kindly ask you to simply provide a link to this repository.
