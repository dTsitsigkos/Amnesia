# Amnesia

Main website: https://amnesia.openaire.eu/

Download app for windows or linux: https://amnesia.openaire.eu/download.html

**The code shared here is the whole code of Amnesia back-end, i.e., the anonymization engine. Amnesia has a clear distinction between the GUI (which is in javascript) and the back end engine, which is in Java. Unfortunately, the current Amnesia version uses a commercial library for the GUI (we plan to change this in the future) and we cannot share the code. The whole anonymization process is performed in the backend and it communicated with the GUI through a ReST API. The backend can be used through ReST calls or command line. The engine provided here is always the full and the most updated version of Amnesia.**

**To successfully build the Amensia engine follow these steps:**

1. Clone the project
2. Go to controller/AppCon.java and set the variable "os" with desired operation system ("windows" or "linux")
3. Build the project
4. Take the .jar from the "scr/target" directory
5. Run via terminal java -Xms1024m -Xmx4096m -Dorg.eclipse.jetty.server.Request.maxFormKeys=1000000 -Dorg.eclipse.jetty.server.Request.maxFormContentSize=1000000 -jar  "path_to_jar_file" --server.port=8181
    
The code above will run the Amnesia engine as a service listening to port 8181 (you can change this) with 1G initial main memory and 4G maximum main memory. Feel free to change these according to your needs. We recommend to use at least the aforementioned amount of memory.

Using Amnesia

## Anonymization via curl
**/anonymizedata [POST]**
This web service is responsible for anonymizing a dataset using template. There are two different ways to call this web service. The first one is, when you have fill out the template file and you would like to anonymize a dataset file. While the second one is, when you don’t have the template, and you would like to create and download the template file.

- In the first case, the arguments for the web services are:

     1. files: the absolute path of all the files, that you would like to upload (originaldataset file, template file and hierarchy files). There are some restrictions in this argument, the first file must be the original dataset and the second file must be the template file and then all the hierarchy files.
     2. del: the split delimiter
After the execution of the web service, the anonymized dataset is stored in a file, with the name of your choice.
    Example via terminal:
    curl -s --form files=@/data/amnesiaAPI/data1/newData.txt --form     files=@/data/amnesiaAPI/template.txt --form     files=@/data/amnesiaAPI/data1/distinct_hier_salary.txt --form     files=@/data/amnesiaAPI/data1/distinct_hier_age.txt --form del=, --out ./(name of output     file) http://localhost:8181/anonymizedata

- In the second case, the arguments are:
  1. files: only the absolute path of the original dataset.
  2. del: the split delimiter
After the execution of the web service, the template file is stored in a file, name of your choice.
    Example via terminal:
    curl -s --form files=@/data/amnesiaAPI/data1/newData.txt --form del=, --out ./(name of template file) http://localhost:8181/anonymizedata

## Invoke Amnesia via command line
To facilitate the use of the Amnesia engine through command line, we have created a script that performs the complete anonymization of a datafile through a single function call. All anonymization parameters are provided through an anonymization template. The script can be found in the current repository as AmnesiaApi.sh
Invoking the script with an instantiated template, where all anonymization parameters have been set, will result to an anonymized dataset. 
In this case, the arguments for the web services are:
del: split delimiter
d: absolute path of the dataset
t: absolute paths of the template and hierarchies
    The output of this web service is a success message and downloads the anonymized dataset, which is stored in a file, with the name of your choice.
    Example via terminal:
    ./amnesiaApi.sh -del “delimiter” -d path/to/dataset -t path/to/filled/template     path/to/hierarchy1 path/to/hierarchy2 –out (name of output file)
If the script is invoked with the 3rd argument then it creates an empty template to be filled by the user. In this case, the arguments are:
del: split delimiter
d: absolute path of the dataset
    The output of this web service is a success message and downloads the template file, which is stored in a file, with the name template.txt.
    Example via terminal:
    ./amnesiaApi.sh -del “delimiter” -d path/to/dataset –out (name of template file)
The big difference between the API and the script is that the script has error handling, which is very helpful. 

## Template Specification
The script of the amnesia API is separated in two procedures, as we mention above. In the first function the user asks for a specific dataset to create a template, in which the user give the appropriate information about the dataset and how the algorithms of the amnesia will handle the dataset in order to anonymize it. So, in the second procedure the user gives the dataset, the filled template and the appropriate hierarchies in order to anonymize the data.

**1st functionality: Creating the template**
  The downloaded template has the below form: 
  ////////////////////// check columns, vartypes /////////////////////////////

  columName1: true,string
  columName2: true,int
  columName3: true,string
  columName4: true,decimal
  columName5: false,string
  columName6: true,int

  //////////////////// END ////////////////////////////////////////////

  /////////////////// set k /////////////////////////////////////

  k:

  As we can observe the template contains all the column names followed by varied values. The first is a Boolean value which indicates if the specific column will participate in the anonymization procedure. The second value is the type (int, decimal, string, date) of the data of the specific column. Also, there is an optional third value which is the name of the hierarchy that will be applied in the specific column. Lastly, the user must set the value of k.

**2nd functionality: Anonymize the data**
  parameters: the path of the dataset, the delimiter of the dataset, the path of filled template, a list of paths of the appropriate hierarchy files and the path of the file where the anonymized data are going to be downloaded

  Example of the command line rule
  ./amnesiaApi.sh -del “delimiter” -d path/to/dataset -t path/to/filled/template path/to/hierarchy1 path/to/hierarchy2 –out path/to/anonymized/data (the last file path may not exist and it will be automatically created)

  Example of filled template

  ////////////////////// check columns, vartypes /////////////////////////////

  columName1: true,string,hierarchy1name
  columName2: true,int
  columName3: true,string,hierarchy2name
  columName4: true,decimal
  columName5: false,string
  columName6: true,int

  //////////////////// END ////////////////////////////////////////////

  /////////////////// set k /////////////////////////////////////

  k: 3

Explanation: After -del the user puts the delimiter of the dataset (. , ! etc.), in the case of the semicolon (;) the user must put the “s” character because of the fact that the terminal recognises the semicolon as seperator of the command line rule. Then after -d and -t the path of the dataset and of the template must be put respectively and finally after –out the user must put the location and the filename where the anonymized data will be downloaded e.g –out /home/exampleuser/downloads/anomData.txt.




## Version History

**version 1.2.3 (release date: 26/01/2021)**
- New feature, autogeneration masking based hierarc for strings

- Fixed bug in date formatting

- Fixed bug in dataset loading

**version: 1.2.2**

- Increased upload file size

- Bugs fixed in hierarchy section

- Bug fixed in date values localization

**version: 1.2.1**

- Optimized clustering disk based algorithm.

- Better handling for "(null)" hierarchy node for unspecified values e.g. empty cells.

- Bug fixed in km-anonymity algorithm.

- Bug fixed in online version.

- Bug fixed for non-english character set.

- Bug fixed for parsing dates with specific localization.

**version: 1.2.0**

- Checked values existence in hierarchy tree.

- Fixed bug on Safari browser.

- Fixed bugs on hierarchy's operations (add, edit, delete).

- Fixed bug on suppression functionality.

- Better handling of the empty cells for range type of hierarchy.

- Fixed bug for windows OS related on java version.

- New clustering disk based algorithm.

- Setted limits on dataset's records and on hierarchy's nodes for on-line version.

- Faster loading of date values in dataset.

- Fixed bug for decimal values with comma.

- Changed loading data-types.

- Fixed bug on rage date type with the same bounds.

**version: 1.1.1**

- Increased the Java heap size.

- Fixed a bug causing crushed in the windows' version of Amnesia.

- The on-line version of Amnesia is restricted to 3 minutes of processing time per anonymization task.

- Fixed bug with characher encoding on several Linux distributions.

**version: 1.1.0**

- New algorithm for Object-relational dataset (combination between relational dataset and set-valued dataset).

- Update error handling.

- Bugs fixed in hierarchy editing.

- Hierarchy removing.

- Bugs fixed when the results are appeared.

**version: 1.0.7**

- Updated interface.

- Several bugs fixed.

**version: 1.0.6**

- Template implementation via terminal.

- Template implementation via rest service.

**version: 1.0.5**

- Date hierarchy editing.

- Updated interface.

- Several bugs fixed.

**version: 1.0.4**

- Wizard for numeric ranges has been updated.

- Upgraded scalability capabilities (Bigger files, can now be uploaded).

**version: 1.0.3**

- Bugs fixed in suppressing and km-anonymity algorithm.

- Changes in text descriptions.

**version: 1.0.2**

- Bugs fixed in front-end and back-end.

**version: 1.0.1**

- Better inteface in pie graph.

- Changes in suppressing.

- Hierarchy editing.

- Bugs fixed.

**version: 1.0.0**

- Initial release.

