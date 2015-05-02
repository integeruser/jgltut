## Learning Modern 3D Graphics Programming with LWJGL
This project is a port of *[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html)* tutorials to Java using [LWJGL](http://www.lwjgl.org), distributed in the hope that it will be useful (original C++ source code [here](https://bitbucket.org/alfonse/gltut/wiki/Home)). The tutorials can be found in the package `jgltut`.  
This project also includes a port of the *[Unofficial OpenGL SDK](https://bitbucket.org/alfonse/unofficial-opengl-sdk/wiki/Home)*, which contains a small math library (derived from the [GLM math library](http://glm.g-truc.net)), a DDS texture loader and other useful stuff, widely used throughout the tutorials. This stuff is collected in the package `jglsdk`.  

To try the tutorials without building the source code, you can download the runnable JAR included in the [Releases](https://github.com/integeruser/jgltut/releases) section. Several tutorials print messages to console: run the JAR from the command line with `java -jar jgltut.jar` to view the output. This can also be useful to read error messages.

To suggest a feature, report bugs, inconsistencies with the original tutorials or general discussion use the [issue tracker](https://github.com/integeruser/jgltut/issues).

Happy coding! :smile:  
Francesco

## Usage
I have included in this repository all the files needed to run the tutorials, so you don't have to download anything else (except the LWJGL library). To correctly compile the code, you will need (at least):

- Java SE Development Kit 7
- LWJGL 2.9.*

Working with previous versions of these software requires minor adjustments to the code.

To get the code running just do the usual steps needed by any LWJGL application:

1. Clone this repository :+1:;
2. Using your favorite IDE, create a new Java project and import the source code of the repository just cloned;
3. Configure the project settings in your IDE options (extracted from [lwjgl.org](http://www.lwjgl.org/guide)):
    - Add the LWJGL JARs to the classpath (usually done by setting up a library dependency for your project and attaching JARs to it);
    - Set the `-Djava.library.path=path/to/natives` as a JVM launch argument or, alternatively, simply copy all the files from the LWJGL `natives` folder to the root folder of your project. Failing to link the natives will result in `Exception in thread "main" java.lang.UnsatisfiedLinkError: no lwjgl in java.library.path`.

Run the `main` method of the first tutorial `jgltut.tut01.Tut1` and check the output in the console window: it should only display the name of the running tutorial and the OpenGL version supported by your video card. If no errors appear, then you're done and you can start exploring other tutorials by running `TutorialChooser.java`. To quit any tutorial simply press `ESC`.

## Notes
I decided to keep the ported code as similar as possible to the original C++ code, despite I would have done some things differently; hence, variables and functions are almost identical to their counterparts in the original projects. I also decided to keep the same project layout:
```
jgltut/
|-- data/
|-- framework/
|-- tut01/
|------ Tut1.java
|-- tut02/
|------ data/
|------ FragPosition.java
|------ VertexColor.java
|-- ...
|-- tut17/
|------ data/
|------ CubePointLight.java
|------ DoubleProjection.java
|------ ProjectedLight.java
|-- LWJGLWindow.java
|-- TutorialChooser.java
```
Each tutorial loads the files it needs from the global `data` folder or from its own `data` folder. The `framework` package contains utility code needed by multiple tutorials. The class `LWJGLWindow.java` is used to create and display a LWJGL window, and `TutorialChooser.java` is a handy program for quickly executing the various tutorials.  
At first, the code may appear difficult to read, but after a bit of reading you will realize that it is well-grouped into logical sections. I suggest you to start by skimming through the code with methods and inner classes folded. Don't get lost in the details of the `jglsdk` module (which is quite big and boring), and remember that the interesting code is the one contained in the tutorials.

If your graphics card does not meet the minimum requirements, running any tutorial will print to the console the message `You must have at least OpenGL 3.3 to run this tutorial.`. The requirements are checked in `LWJGLWindow.java` using the LWJGL function `GLContext.getCapabilities().OpenGL33`.

I can't dedicate much time to this project anymore, but in the future I will probably:

- keep refactoring code I don't like;
- rework some parts of `jglsdk`;
- update the code to work with LWJGL 3 (when it will be officially released).

## License
This project is licensed under the [Attribution 4.0 International license](http://creativecommons.org/licenses/by/4.0/): you can do what you want with my code, but if you want to publish some derived work I kindly ask you to simply provide a link to this repository.

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
