# hibernate-envers-tools

## Installation :

1. Download [latest snapshot version](https://github.com/hydra1983/hibernate-envers-tools/tree/mvn-repo/snapshots/com/wds/tools/hibernate-envers-tools/3.6.10-SNAPSHOT) (zip)
2. Unzip the downloaded file as {root}/hibernate-envers-tools and add {root}/hibernate-envers-tools to system path

## Usage :

```shell
usage: envers [(-v | --verbose)] <command> [<args>]

The most commonly used envers commands are:
    help      Display help information
    install   Enable change tracking for target database using Hibenrate Envers
    version   Show version

See 'envers help <command>' for more information on a specific command.

```

## Example (win):
1. add required libraries, such as "h2.jar", to {root}/hibernate-envers-tools/lib
2. run command as below:

```shell
envers install -v ^
  --url=jdbc:h2:tmp/example ^
  --username=sa ^
  --password= ^
  --basepackage=com.wds.demo.sync.domain
```
