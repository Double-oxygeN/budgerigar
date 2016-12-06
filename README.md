# budgerigar

BUDGERIGAR: my versatile bulletin

## Installation

Download from https://github.com/Double-oxygeN/budgerigar.

## Usage

    $ java -jar budgerigar-0.1.0-SNAPSHOT-standalone.jar

It receives json string using UNIX Domain Socket(/private/var/tmp/budgerigar.socket).
- `x`, `y` : position of the up-left corner of the rectangle (null means random position)
- `width`, `height` : width and height of the rectangle (null means random value)
- `frame-width` : frame width (null means 2)
- `color` : color of the rectangle
  - `frame` : frame color (null means the same as body color)
  - `body` : body color (null means white)
  - `character` : character color (null means black)
- `body` : text in the rectangle
- `font` : font (null means "YuGo-Medium")
- `font-size` : font size (null means 12)
- `fade` : speed of fading (null means 0.1)

## License

Copyright © 2016 Double_oxygeN

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
