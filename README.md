Learning Modern 3D Graphics Programming with LWJGL
--------------------------------------------------
This project is a port of *[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html)* tutorials to Java using LWJGL and it is distributed in the hope that it will be useful (original C++ source code [here](https://bitbucket.org/alfonse/gltut/wiki/Home)). The tutorials can be found in the package `fcagnin.jgltut`.  
In this project is also included a port of the *[Unofficial OpenGL SDK](https://bitbucket.org/alfonse/unofficial-opengl-sdk/wiki/Home)*, which contains a simple math library, a DDS texture loader and other useful stuff, widely used throughout the tutorials. This stuff is collected in the package `fcagnin.jglsdk`.

I have included in this repository all the files needed to run the tutorials so you don't need to download anything else (except the LWJGL library).

To suggest a feature, report a bug (please do it!), or general discussion use the [issue tracker](https://github.com/integeruser/jgltut/issues).

Happy coding! :smile:  
Francesco

Usage
-----
To correctly compile the code, you will need (at least):

- Java SE Development Kit 7
- [LWJGL 2.9.1](http://sourceforge.net/projects/java-game-lib/files/Official%20Releases/LWJGL%202.9.1/)

Working with previous versions of these software requires minor fixes to the code.

Common steps to get the code running:

1. Clone this repository :+1:
2. Using your favorite IDE, create a new Java project and import the source code of the repository just cloned
3. Configure the project settings in your IDE options:
    - set the project language level to `7.0 - Diamonds, ARM, multi-catch etc.` (may not be needed)
    - add the LWJGL jar `lwjgl-2.9.1/jar/lwjgl.jar` to the project libraries
    - link the appropriate LWJGL natives for your operating system: this really depends on your IDE. The easiest method I have found is to simply copy all the files contained in the LWJGL native folder (e.g. `lwjgl-2.9.1/natives/windows/`) in the root folder of your project.  
    Another option is to set `-Djava.library.path=path/to/natives` as VM option in the IDE.  
    Failing to link the natives will result in `Exception in thread "main" java.lang.UnsatisfiedLinkError: no lwjgl in java.library.path`
4. Run the main method of the first tutorial `fcagnin.jgltut.tut01.Tut1` and check for error messages in the console; if nothing shows up then everything is working properly.

Notes
-----
I decided to write the code of this project as similar as possible to the original C++ code, despite I would have done some things differently. Variables and functions are almost identical to their C++ counterpart. At first, the code may be difficult to read, but with a bit of practice you will realize that the code layout is organized to find the various sections quickly. I suggest you to start reading each class with all methods and inner classes folded.

If your graphics card does not meet the minimum requirements to run the tutorials, the program will print to console the message `You must have at least OpenGL 3.3 to run this tutorial.`, and then will likely crash raising an exception. The requirements are checked using LWJGL functions, and the program simply reports what the LWJGL library finds.

I am not able to run the tutorials on a MacBook Pro (Retina, Late 2013), as LWJGL reports that the graphics card does not support OpenGL 3.3 (despite it should support OpenGL 4.1); I can't tell if this is a problem of LWJGL or OS X. I tried both in Mavericks and Parallels+Windows. Suggestions are welcome. 

I can't dedicate much time to this project anymore, but in the future I will probably:

- do some refactoring on code I don't like
- rework some parts of `fcagnin.jglsdk`
- update the code to work with LWJGL 3 (when it will be officially released)

License
-------
This project is licensed under the [Attribution 4.0 International license](http://creativecommons.org/licenses/by/4.0/).

The LWJGL license can be found [here](http://lwjgl.org/license.php).
The gltut and glsdk licenses can be found [here](https://bitbucket.org/alfonse/gltut/raw/3ee6f3dd04a7/License.txt) and
[here](https://bitbucket.org/alfonse/unofficial-opengl-sdk/raw/1893b6e851b9/License.txt).

The following files are copywritten and distributed under the Creative Commons Attribution 3.0 Unported (CC BY 3.0) license. Attribution for these works is presented here:

Attributed to Etory, of OpenGameArt.org:

- src/main/java/fcagnin/jgltut/data/seamless\_rock1_small.dds

Attributed to p0ss, of OpenGameArt.org:

- src/main/java/fcagnin/jgltut/data/concrete649_small.dds
- src/main/java/fcagnin/jgltut/data/dsc\_1621_small.dds
- src/main/java/fcagnin/jgltut/data/rough645_small.dds