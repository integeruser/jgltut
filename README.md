OpenGL tutorials with LWJGL
=================

Hi all!
We are right OpenGL beginners like a lot of you, and we know how frustrating is not find any good resources on modern OpenGL programming techniques, so
we are proud to start this project for porting McKesson's tutorials to Java with LWJGL.
This is the book where to start: http://www.arcsynthesis.org/gltut/index.html
We decided to put online our work, although it is far from complete, to give to all LWJGL users a nice starting point for the understanding of OpenGL 3.3.
We are dedicating to this project most of our free time and it will update frequently.
Best regards,

Francesco & Marco



Project information
-------------------

The project is developed with Java 1.7 and LWJGL 2.8.2, and may not work with older versions.
We decided to port the tutorials in a manner as similar as possible to the original, so some tutorials arent't written in 'Java-style' but this isn't a big problem :)

* "rosick/framework" : package with common classes.
* "rosick/glm"       : package with math classes.
* "rosick/glutil"    : package with opengl-related classes.
* "rosick/mckesson"  : package with LWJGL porting of Jason McKesson's tutorials.

To try our work, we recommend using Eclipse ide. Open Eclipse, copy or link the "rosick" package into an existing project (in the "src" folder) and, if you haven't already done, 
add and configure the LWJGL library (visit http://www.lwjgl.org/ for further information) in the project build path. Open any tutorial and just click the play button.


Github users
------------

I kindly ask github users not to pull requests at this time.
We want to develop this project with our hands and if possibly to translate every tutorial of the book.
If we are not be able to continue working on this project, then we will be happy to let each user contributes with their code.
On the other hand, we ask you to communicate us every issue or difficulty that you have found in this project.
Thanks in advance.


Distribution
------------

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.