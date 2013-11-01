# hibernate-envers-tools

## Installation :

1.Download lasted binary snaphot version from [maven repository](https://github.com/hydra1983/hibernate-envers-tools/tree/mvn-repo/snapshots/com/wds/tools/hibernate-envers-tools/3.6.10-SNAPSHOT)

2.Unzip and add the it to system path

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

```shell
envers install -v ^
  --url=jdbc:h2:tmp/example
  --username=sa
  --password=
  --basepackage=com.wds.demo.sync.domain
```
