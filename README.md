# osu2jml
A tool to convert osu! beatmaps into juggling patterns renderable in [Juggling Lab](https://github.com/jkboyce/jugglinglab).

![Example_osu2jml](https://github.com/Kcits970/osu2jml/assets/93324991/c5a78bd0-01cd-4cd9-91f3-56b867f019bf)

Example overlay: _(This part is manually done in a video editor.)_

![Comp 1_1](https://github.com/Kcits970/osu2jml/assets/93324991/e42d3445-ccad-4bb5-9b2a-7fbbff4b33bc)

## Setup
1. Install the Java 21 Development Kit. (https://www.oracle.com/kr/java/technologies/downloads/)
2. Download the v1.0 binary release.
3. Open the terminal, and change directory to the location of osu2jml.jar.
4. Run the program with the following command:

```
java -jar osu2jml.jar <<BEATMAP_FILE_LOCATION>> <<JML_OUTPUT_LOCATION>>
```

## Additional Parameters
It is possible to provide extra parameters to the program.

| Argument Name | Description | Default Value | Example Usage |
|-----------|-----------|-----------|-----------|
| -ss | siteswap | 3 | -ss=531 |
| -m | beatmap modifier | | -m=ezdt |
| -h | hand sequence | LR | -h=LLLR |
| -f | filler duration (seconds) | 1 | -f=3 |
| -c | prop color | white | -c=green |
| -s | prop color saturation | 1 | -s=0.75 |
| -i | invert prop colors | false | -i=true |

## Extra Details on Argument Parameters
### Siteswap
For siteswap parameters, instead of using letters, it is possible to wrap the number between curly braces and use that instead.
For example, `-ss=db97531` is equivalent to `-ss={13}{11}97531`.
The siteswap argument only accepts asynchronous siteswaps (this includes async multiplex patterns).

### Beatmap Modifier
This argument only accepts some combination of "ez", "hr", "ht", "dt". Visual mods such as "hd" or "fl" are not accepted.

### Hand Sequence
The hand sequence argument specifies which hand will hit the beatmap hit object.
For example, `-h=LLLR` means that the first, second, and third hit object will be 'hit' by the left hand, and the fourth hit object by the right hand. (fifth, sixth, seventh by the left hand, eighth by the right hand, and so on...)

### Prop Color
The prop color accepts a special parameter `rainbow`. This parameter will give the props a 'rainbow' color.

## Example Commands
```
java -jar osu2jml.jar <<BEATMAP_FILE_LOCATION>> <<JML_OUTPUT_LOCATION>> -ss=[41][21][31] -c=rainbow
java -jar osu2jml.jar <<BEATMAP_FILE_LOCATION>> <<JML_OUTPUT_LOCATION>> -ss=12345 -c=red -s=0.5 -m=hrdt
java -jar osu2jml.jar <<BEATMAP_FILE_LOCATION>> <<JML_OUTPUT_LOCATION>> -c=white -i=true -f=3
```
