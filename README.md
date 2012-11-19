OpenGL 3.3 tutorials with LWJGL
===============================
This project is a port of *[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html)* tutorials to Java using LWJGL and it is distributed in the hope that it will be useful. You can find the original c++ source code [here](https://bitbucket.org/alfonse/gltut/wiki/Home).  


The tutorials use many features of *[Unofficial OpenGL SDK](https://bitbucket.org/alfonse/unofficial-opengl-sdk/wiki/Home)*, and you can find my port of this sdk [here](https://github.com/integeruser/jglsdk). It has a simple math library, a DDS texture loader and other useful stuff. I have included in this project a version of jglsdk
so you don't need to download it.


Tutorials ported so far: 17.3 of 17.3


If you don't want to download the source code, you can play with the tutorials with a runnable jar i uploaded in [the download section](https://github.com/integeruser/gltut-lwjgl/downloads). In several tutorials you can interact using the keyboard: key mappings are written at the top of each tutorial .java file.


To suggest a feature, report a bug (please do it!), or general discussion please use the [issue tracker](https://github.com/rosickteam/OpenGL/issues).  
Happy coding! :)

Francesco



General information
-------------------
The project uses features of Java 7 and doesn't work with older versions. It is tested with LWJGL 2.8.5.  
To download and install LWJGL visit the [official site](http://www.lwjgl.org/). 

- "rosick/jglsdk" 	: package with the [glsdk port](https://github.com/integeruser/jglsdk).
- "rosick/mckesson" : package with the tutorials port.

To test it out, it is recommended to use [Eclipse](http://www.eclipse.org/).  
Open Eclipse, copy or link the "rosick" package into an existing project (e.g. in the "src" folder) and, if you haven't already done, 
add and configure the LWJGL library in the project build path. Open any tutorial and run it by clicking the 'play' button.

I have tested my code only on Windows.



Distribution
------------
This project is licensed under a [Creative Commons Attribution 3.0 License](http://creativecommons.org/licenses/by/3.0/): you are free to use and modify this code, 
but i ask you to put a link back to this repository in the credits or documentation of your projects.

  
The LWJGL license can be found [here](http://lwjgl.org/license.php).
The gltut and glsdk licenses can be found [here](https://bitbucket.org/alfonse/gltut/raw/3ee6f3dd04a7/License.txt) and 
[here](https://bitbucket.org/alfonse/unofficial-opengl-sdk/raw/1893b6e851b9/License.txt).