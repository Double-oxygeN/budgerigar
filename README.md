# budgerigar

BUDGERIGAR: my versatile bulletin

## Installation

Download from https://github.com/Double-oxygeN/budgerigar.

## Usage

It receives json string using UNIX Domain Socket(/private/var/tmp/budgerigar.socket).
- `x`, `y` : position of the up-left corner of the rectangle (null means random position)
- `width`, `height` : width and height of the rectangle (null means random value)
- `frame-width` : frame width (null means 2)
- `color` : color of the rectangle
  - `frame` : frame color (null means the same as body color)
  - `body` : body color (null means white)
  - `character` : character color (null means black)
- `body` : contents of the rectangle (write xml)
- `fade` : speed of fading (null means 0)

## Options

...

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2016 Double_oxygeN

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
