Learning Modern 3D Graphics Programming with LWJGL
--------------------------------------------------
This project is a port of *[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html)* tutorials to Java using LWJGL and it is distributed in the hope that it will be useful (original C++ source code [here](https://bitbucket.org/alfonse/gltut/wiki/Home)). The tutorials can be found in the package `fcagnin.jgltut`.  
In this project is also included a port of the *[Unofficial OpenGL SDK](https://bitbucket.org/alfonse/unofficial-opengl-sdk/wiki/Home)*, which contains a simple math library, a DDS texture loader and other useful stuff, widely used throughout the tutorials. This stuff is collected in the package `fcagnin.jglsdk`.

I have included in this repository all the files needed to run the tutorials so you don't need to download anything else (except the LWJGL library).

To suggest a feature, report a bug (please do it!), or general discussion use the [issue tracker](https://github.com/integeruser/jgltut/issues).

Happy coding! :smile:  
Francesco

Installation
------------
This project uses features of Java 7 and doesn't work with older versions. It is tested with LWJGL 2.8.5 but it should also work properly with more recent releases.

1. Download the sources of this project and import them into your favorite IDE: you may have to set the project language level to 7.0 and mark /src/main/java as a sources root folder;
2. [Download and set up LWJGL](http://lwjgl.org/wiki/index.php?title=Downloading_and_Setting_Up_LWJGL): be sure to configure in your IDE both the location of lwjgl.jar and the location of the natives;
3. Run fcagnin.jgltut.TutorialChooser and check if everything works properly.

Updates
-------
I don't have much time to dedicate to this project anymore, but in the next weeks (months?) i want to:

- do some refactoring / fomatting: convert tabs to spaces, fix some typos, remove a lot of blank lines and other minor stuff;
- rework some parts of jglsdk;
- update the code to work with lwjgl3 (when it will be officially released).

Distribution
------------
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