# GUI

## Some Design Choices
* [Processing](/https://processing.org/) JVM based dialect that allows for easy drawing of shapes
    * uses [JOGL](https://jogamp.org/jogl/www/) to allow for easy rendering of 3D and 2D shapes, according to our design
      goal
    * Already has native libs set up for easy use across platforms
* [PeasyCam](https://mrfeinberg.com/peasycam/) camera controls for 3D views, far easier than rolling your own
* We roll everything GUI related into a Fat Jar, as processing dependency management is intentionally basic

## GUI Design Inspired By Existing Libs
* [ControlP5](https://github.com/sojamo/controlp5)
* [G4P](http://www.lagers.org.uk/g4p/)
* [LazyGui](https://github.com/KrabCode/LazyGui)