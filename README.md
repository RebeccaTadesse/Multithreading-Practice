This was a problem set for object-oriented programming.
*******

This implements client-server multithreading in order for multiple clients to interact with a 
server and simultaneously edit a drawing.

## To Run:
Open in your preferred IDE. Open the terminal.

First, run SketchServer.java. Now the server can listen for incoming connections from the editor (client).

Now open another terminal and run the main class Editor.java. A graphical editor should open, providing you with the option to draw shapes (ellipses, rectangles, lines), move them, recolor them et cetera. To test multithreading capabilities, run Editor.java a second time. As you edit one graphical editor, the other should reflect that change concurrently.

You can open multiple editing windows (I have tested at least five graphical editors at a time; feel free to try even more!)

Note: Ensure that you have Java version 16 or higher.
