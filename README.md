Learning Modern 3D Graphics Programming with LWJGL
--------------------------------------------------
This project is a port of *[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html)* tutorials to Java using LWJGL and it is distributed in the hope that it will be useful (original C++ source code [here](https://bitbucket.org/alfonse/gltut/wiki/Home)). The tutorials can be found in the package `fcagnin.jgltut`.  
In this project is also included a port of the *[Unofficial OpenGL SDK](https://bitbucket.org/alfonse/unofficial-opengl-sdk/wiki/Home)*, which contains a small math library (derived from the [GLM math library](http://glm.g-truc.net/)), a DDS texture loader and other useful stuff, widely used throughout the tutorials. This stuff is collected in the package `fcagnin.jglsdk`.

I have included in this repository all the files needed to run the tutorials so you don't need to download anything else (except the LWJGL library).

To suggest a feature, report bugs / inconsistencies with the original tutorials, or general discussion use the [issue tracker](https://github.com/integeruser/jgltut/issues).

Happy coding! :smile:  
Francesco

Usage
-----
To correctly compile the code, you will need (at least):

- Java SE Development Kit 7
- [LWJGL 2.9.1](http://sourceforge.net/projects/java-game-lib/files/Official%20Releases/LWJGL%202.9.1/)

Working with previous versions of these software requires minor adjustments to the code.

Common steps to get the code running:

1. Clone this repository :+1:
2. Using your favorite IDE, create a new Java project and import the source code of the repository just cloned
3. Configure the project settings in your IDE options:
    - set the project language level to `7.0 - Diamonds, ARM, multi-catch etc.` (may not be needed)
    - add the LWJGL jar `lwjgl-2.9.1/jar/lwjgl.jar` to the project libraries
    - link the appropriate LWJGL natives for your operating system: the easiest method I have found is to simply copy all the files from the LWJGL native folder (e.g. all the files in `lwjgl-2.9.1/natives/windows/`) to the root folder of your project.  
    Alternatively, try setting `-Djava.library.path=path/to/natives` as VM option in the IDE, or search Google for instructions.  
    Failing to link the natives will result in `Exception in thread "main" java.lang.UnsatisfiedLinkError: no lwjgl in java.library.path`
4. Run the `main` method of the first tutorial `fcagnin.jgltut.tut01.Tut1` and check for error messages in the console: it should only display the name of the running tutorial and the OpenGL version supported by your video card.

Notes
-----
I decided to write the code of this project as similar as possible to the original C++ code, despite I would have done some things differently; variables and functions are almost identical to their C++ counterparts. I also decided to keep same the project layout:
```
jgltut
|--- data
|--- framework
|--- tut01
|------ Tut1.java
|--- tut02
|------ data
|------ FragPosition.java
|------ VertexColor.java
|--- ...
|--- tut17
|------ data
|------ CubePointLight.java
|------ DoubleProjection.java
|------ ProjectedLight.java
|--- LWJGLWindow.java
|--- TutorialChooser.java
```
The only files I have added are `LWJGLWindow.java` (simple class that wraps all the code needed to create and display a LWJGL window) and `TutorialChooser.java` (a handy program used to run the various tutorials).  
Each tutorial has its own `main` method that can be used to run it. Alternatively, you can execute the `main` method of `TutorialChooser.java` and then click on the name of the tutorial you want to run.  
At first, the code may be difficult to read, but with a bit of practice you will realize that the code layout is organized to find the various sections quickly. I suggest you to start reading each class with all methods and inner classes folded.

If your graphics card does not meet the minimum requirements, running a tutorial will print to the console the message `You must have at least OpenGL 3.3 to run this tutorial.`, and the program will likely crash raising an exception. These requirements are checked in `LWJGLWindow.java` when the `Display` is created, using `GLContext.getCapabilities().OpenGL33`.

I can't dedicate much time to this project anymore, but in the future I will probably:

- do some refactoring on code I don't like
- rework some parts of `fcagnin.jglsdk`
- update the code to work with LWJGL 3 (when it will be officially released)

License
-------
This project is licensed under the [Attribution 4.0 International license](http://creativecommons.org/licenses/by/4.0/): you can do what you want with my code, but if you want to publish some derived work I kindly ask you to simply provide a link to this repository.

Credits
-------
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
