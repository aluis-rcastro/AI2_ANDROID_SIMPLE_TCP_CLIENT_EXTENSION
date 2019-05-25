# AI2_ANDROID_SIMPLE_TCP_CLIENT_EXTENSION
Simple TCP Client to send `one-shot` commands to remote server and get its response, closing connection afterward

Attached, there is a simple TCP client extension <img src="https://github.com/aluis-rcastro/AI2_ANDROID_SIMPLE_TCP_CLIENT_EXTENSION/blob/master/res/TCP.png" alt="" width="16" height="16"> that implements a very simple socket; note that its operation is performed 'one-shot', which means that only one command is sent ( and its respective response, read ) at a time, and then the socket connection is closed. So, be aware that it is not a generic "shell" implementation, therefore not keeping always connected. Another point to remark is that connection is not allways checked, which means that data content are not handled during reception, although defined a timeout on the socket for the lack of response within specific interval.

Follow the screenshot of the test program, also attached:

<img src="https://github.com/aluis-rcastro/AI2_ANDROID_SIMPLE_TCP_CLIENT_EXTENSION/blob/master/res/Mobile.png" alt="" width="168" height="344">

The following is an overview of the collection of the component primitives:

<img src="https://github.com/aluis-rcastro/AI2_ANDROID_SIMPLE_TCP_CLIENT_EXTENSION/blob/master/res/Components.png" alt="" width="244" height="333">

And below, the program itself, also attached. Note that the highlighted item, while occupying an expressive part of the project, is not functional but only informative.

<img src="https://github.com/aluis-rcastro/AI2_ANDROID_SIMPLE_TCP_CLIENT_EXTENSION/blob/master/res/Project.png" alt="" width="555" height="333">

Feel free to conduct experiments and give feedback.
