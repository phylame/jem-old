# pw-books toolkit
A collection of tools for reading/writing/manipulating e-book files.

#### pw-books consists of some parts:
* a core (**jem-core**) that reads/writes e-books
* a console tool named **scj** manages e-books
* a swing-based book editor named **imabw**

#### To use the jem-core, the following are reauired:
* commons-logging 1.2+

#### scj resuired:
* jem-core 2.0+
* commons-cli 1.2+

#### imabw required:
* jem-core 2.0+

## Command line examples
Convert UMD book to ePub and set new title, author

    java -jar scj-*.jar input.umd -c epub -o output.epub -Stitle=Example -Sauthor=PW

View attributes of UMD book and its chapter

    java -jar scj-*.jar input.epub -Vtitle -Vpublisher -Vchapter1$title

View table of contents (TOC) of ePub book
    
    java -java scj-*.jar input.epub -Vtoc
