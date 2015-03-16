# scj - SCI (Simple Console Interface) for Jem
A console tool to process e-books.

## Required
* pw-core 2.0+
* commons-cli 1.2+

## Command line examples
Convert UMD book to ePub and set new title, author

    java -jar scj-1.0.jar input.umd -c epub -o output.epub -Stitle=Example -Sauthor=PW

View attributes of UMD book and its chapter content

    java -jar scj-1.0.jar input.epub -Vtitle -Vpublisher -Vchapter1$title

View table of contents (TOC) of ePub book

    java -java scj-1.0.jar input.epub -Vtoc