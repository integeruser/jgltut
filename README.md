OpenGL tutorials with LWJGL
===========================

This project is my porting of McKesson's excellent tutorials to Java using LWJGL.

[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html) is the book where to start. 
I decided to put online my work, although it is not yet complete, to give to all LWJGL users a starting point for the understanding of OpenGL 3.3.
I am dedicating to this project most of my free time and it will update as frequently as possible.
Thanks to Marco for his contribution.

Happy coding! 

Francesco



Project information
-------------------
The project is developed with Java 1.7 and LWJGL 2.8.3, and may not work with older versions.
I decided to code this project in a manner as similar as possible as the original one (which is written in c++), so i used the same folders structure:

* "rosick/framework" : package with common classes.
* "rosick/glm"       : package with math classes from Unofficial OpenGL SDK.
* "rosick/glutil"    : package with utils classes from Unofficial OpenGL SDK.
* "rosick/mckesson"  : package with tutorial classes.

To test it out, i recommend using Eclipse ide. Open Eclipse, copy or link the "rosick" package into an existing project (in the "src" folder) and, if you haven't already done, 
add and configure the LWJGL library (visit [LWJGL home](http://www.lwjgl.org/) for further information) in the project build path. Open any tutorial and run it by clicking the
'play' button.


Porting notes
-------------
Every tutorial class inherits from the base class GLWindow: in this class the main loop is executed (in c++ this is managed by Glut), 
which in order calculates the values of 'elapsedTime' and 'lastFrameDuration', call update() and display(). The function reshape() is called once before the main loop starts and 
everytime the window get resized. The two variables 'elapsedTime' and 'lastFrameDuration' can be accessed by calling their respective getters; update(), display() 
and reshape() are overrided in every tutorial class. 

Not all c++ features are implemented in Java (eg. function pointers), so i had to make some changes to make everything work.
This is the changes list:

* In the original tutorials, Glut manage keyboard inputs by calling keyboard(char c). I choose to create a general purpose method called update(), which is executed 
once per loop, and here check if the keyboard keys that we need are pressed or not.
* In addition, to obtain a more accurate movement (eg. the camera controlled with the keyboard) i computed the simplest 
possible integration by multiplying the deltaMovement by "lastFrameDuration".
* "PushStack" : the author decided to implement a MatrixStack with a push/pop organization based on PushStack's constructor and destructor. 
Unfortunately, in Java objects destructor aren't called at the end of the object's scope, but the destruction is decided by the garbage collector.
Marco choose to create the push() and pop() methods in MatrixStack.java, and call them manually when needed.


Github users
------------

I kindly ask github users not to pull requests at this time.
I want to develop this project with my hands and if possibly to translate every tutorial of the book.
On the other hand, i ask you to communicate issues or difficulties that you have found using this project.
Thanks in advance.



Distribution
------------

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.