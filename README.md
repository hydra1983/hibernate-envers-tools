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

```shell
NAME
        envers install - Enable change tracking for target database using
        Hibenrate Envers

SYNOPSIS
        envers [(-v | --verbose)] install [--basepackages <basepackages>]
                [-D <parameters>...] [--dialect <dialect>] [--driver <driver>]
                [--password <password>] [--revent <revent>] --url <url>
                [--username <username>]

OPTIONS
        --basepackages <basepackages>
            Package names which will be converted to regexp pattern to scan
            entities

        -D <parameters>
            System parameters

        --dialect <dialect>
            Dialect class of target database

        --driver <driver>
            Driver class of target database

        --password <password>
            Password of target database

        --revent <revent>
            Revision entity class

        --url <url>
            Url of target database

        --username <username>
            Username of target database

        -v, --verbose
            Verbose mode
```

## Example (win):
1. add required libraries, such as "h2.jar", to {root}/hibernate-envers-tools/lib
2. run command as below:

```shell
envers  -v install ^
        --url=jdbc:h2:tmp/example ^
        --username=sa ^
        --password= ^
        --basepackages=com.wds.demo.*.domain
```
