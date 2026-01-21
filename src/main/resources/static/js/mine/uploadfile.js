/* 
 * Important Functions  for our application
 * 
 */

//Test fUNCTION
function hello(){
    $.ajax({
        url: "hello",
        type: "POST",
        success : function(result) {
            console.log("everything ok");
            alert("result = " + result);
            //window.location.href ="myhier.html";
        },
        error : function(result){

            alert("Server /hello");
        }
    });      
}


function loadhtml(){
    $('#about_attr').load('about_menu.html',function(){});
    $('#version_noti').load('version_title.html',function(){});
    $('#online-warning').load('myonline-warning.html',function(){});
    $('#copyrights').load('copyrights.html',function(){});
}


function uploaddicom(){
    var inps = document.getElementById("dicomfiles");
    console.log(inps.files);
    
    for(i=0; i<inps.files.length; i++){
        if(inps.files[i].name.endsWith(".dcm")){
            uploadfile2("dicomfile",inps.files[i]);
        }
    }
    window.location.href ="mywizard.html";
}

function uploadfile(id){
    if(id =="datafile" || id =="setfile" || id=="xmlfile" || id=="dicomfile"){
        
        $.ajax({
            url: "/action/getcolumnnames",
            type: "POST",
            success : function(result){
//                alert("Edddddvvv");
                if(result !==null  && result.trim().length !== 0){
                    if(!confirm('The new dataset will replace the existing one')){
                        return;
                    }
                    else{
                        uploadfile2(id,null);
                    }
                }
                else{
                   uploadfile2(id,null); 
                }
            },
            error : function(result){
                uploadfile2(id,null);
            }
        });
    }
    else{
        uploadfile2(id,null);
    }
}


function errorHandling(error){
    $.ajax({
        url: "/action/errorhandle",
        type: "POST",
        data: "error="+error,
        success : function(result) {
            console.log("ok with error log");
        }
    });
}


//Upload File via buttons
function uploadfile2(id, mfile){
    //  /action/upload
    
    
    var oMyForm = new FormData();
    
    var file = null;
    if(mfile === null){
        file = document.getElementById(id).files[0];
    }
    else{
        file = mfile;
    }
    
    var FLAG = false;
    console.log("file = " + file);
    console.log("id = " + id);
    
    if ( file.name.includes(".")){
        var re = ".";
        var filetype = file.name.toString().split(re);
        if(id === "hierarchy" && filetype[filetype.length-1] === "json"){
            FLAG = true;
        }
        else if ( filetype[filetype.length-1] !== "csv" && filetype[filetype.length-1] !== "txt" && id!== "xmlfile" && id!=="dicomfile"){
            FLAG = false;
        } 
        else{
            FLAG = true;
        }
    }
    else{
        FLAG = true;
    }
   
    
    
    if ( FLAG == true){
        if ( id =="datafile" || id =="setfile" || id=="xmlfile" || id=="dicomfile" ){
            
            $( "#spinner" ).show();
            console.log(id);
            oMyForm.append("file", file);
            oMyForm.append("path",null);
            oMyForm.append("data",true);
            $.ajax({
                dataType : "json",
                url : "/action/upload",
                data : oMyForm,
                type : "POST",
                enctype: "multipart/form-data",
                processData: false, 
                contentType:false,
                async : false,
                success : function(result) {
                   
                    if (!result.success){
                        //alert(result.problem);
                        alert("uploadfile:" + result.problem);
                    }
                    else if(id=="dicomfile"){
                        return;
                    }
                    
                    $( "#spinner" ).hide();
                    
                    if ( id == "datafile" || id == "xmlfile" ){
                        window.location.href ="mywizard.html";
                    }
                    else{
                        $.ajax({
                            url: "/action/getsetdata",
                            type: "POST",
                            success : function(result) {
                                if(!result.success){
                                    alert(result.problem);
                                    $( "#spinner" ).hide();
                                }
                                else{
                                    window.location.href ="mydataset.html";
                                }
                            },
                            error : function(xhr, status, error){
                                if(xhr.hasOwnProperty('responseText')){
                                    console.log(xhr.responseText);
                                    console.log(status.toString());
                                    errorHandling(xhr.responseText);
                                    alert("Problem with loading set-valued dataset \n"+error.toString());
                                }
                                else{
                                    alert("Problem with loading set-valued dataset ");
                                }
                                $( "#spinner" ).hide();
                            }
                        });
                    }
                },
                error : function(xhr, status, error){
                    if(xhr.hasOwnProperty('responseText')){
                        console.log(xhr.responseText);
                        console.log(status.toString());
                        errorHandling(xhr.responseText);
                        alert("Problem with loading dataset \n"+error.toString());
                        
                    }
                    else{
                        alert("Problem with internet connection or with size of file");
                    }
                    $( "#spinner" ).hide(); 
                }
            });
        }
        else{
            $( "#spinner" ).show();
            console.log(id);
            oMyForm.append("file", file);
            oMyForm.append("path",null);
            oMyForm.append("data",false);
            $.ajax({
                dataType : "json",
                url : "/action/upload",
                data : oMyForm,
                type : "POST",
                enctype: "multipart/form-data",
                processData: false, 
                contentType:false,
                success : function(result) {
                    if (result.success){
//                        alert(result.problem);
                    }
                    else{
                        alert(result.problem);
                    }
                    
                    if (id == "anonrules"){
                        $.ajax({
                            url: "/action/loadanonymizationrules",
                            type: "POST",
                            data: { filename:file.name },
                            success : function(result) {
                                window.location.href ="myresults.html";
                            },
                            error : function(result){
                                if(result!=null){
                                    alert(result);
                                }
                                $( "#spinner" ).hide();
                            }
                        });
                    }
                    else{
                        console.log("hier edwww");
                        $.ajax({
                            url: "/action/loadhierarchy",
                            type: "POST",
                            data: { filename:file.name },
                            success : function(result) {
                                var res = result.localeCompare("error");
                                if(res == 0){
                                    alert("You must upload hierarchy for range date");
                                }
                                else{
                                    window.location.href ="myhier.html";
                                }
                            },
                            error : function(xhr, status, error){
                                if(xhr.hasOwnProperty('responseText')){
                                    console.log(xhr.responseText);
                                    console.log(status.toString());
                                    errorHandling(xhr.responseText);
                                    try{
                                        var json = JSON.parse(xhr.responseText);
                                        alert("Problem with loading the hierarchy \n"+json.error);
                                        window.location = "index.html";
                                    }catch(Exception){
                                        alert("Problem with loading the hierarchy \n"+error.toString());
                                    }
                                    
                                }
                                else{
                                    alert("Problem with loading hierarchy \n");
                                }
                                
                                $( "#spinner" ).hide();
                            }
                        });   
                    }
                },
                error : function(xhr, status, error){
                    if(xhr.hasOwnProperty('responseText')){
                        console.log(xhr.responseText);
                        console.log(status.toString());
                        errorHandling(xhr.responseText);
                        alert("Problem with loading file \n"+error.toString());  
                        
                    }
                    else{
                        alert("Problem with internet connection or with the size of the file");
                    }
                    $( "#spinner" ).hide(); 
                }
            });
        }
    }
    else{
        alert("The application supports only .csv and .txt files");
        $( "#spinner" ).hide();
    }
}


//Get small sample of the dataset 
function getsmalldataset() {
    var del = document.getElementById("delimiter").value;
    var datatype = document.getElementById("datatype").value;
    var delset = document.getElementById("delimiterset").value;
    
    if(datatype === ""){
        datatype = "dicomfile";
    }
    
    console.log(datatype);
    if ( del == "" && datatype !== "dicomfile"){
        alert("Complete delimiter field");
    }
    else{
        $("#spinner").show();
        $.ajax({
            url: "/action/getsmalldataset",
            type: "POST",
            data: { del: del , datatype:datatype, delset:delset},
            success : function(result) {
                var str;
                
                
                if(result == null){
                    $("#spinner").hide();
                    alert("Parsing Problem in Dataset." + result);
                    restart();
                    window.location = "index.html";
                }
                else if(result.errorMessage == "1"){
                    alert("Parse problem. Different size between title row and data row");
                    window.location.href ="index.html";
                }

                else if ( result !== null){
                    if (datatype ==="tabular" || datatype==="Disk" || datatype === "dicomfile"){
                        str =  "<thead>" +"<tr>" ;
                        
                        for (var i = 0 ; i<result.columnNames.length ; i ++ ){
                            str += "<th style = \"white-space:nowrap;\">" + result.columnNames[i] + " <input  type=\"checkbox\" id=check" + i +" class=\"i-checks\" checked=\"\" ></th>";
                        }
                        str += "</tr>" +"</thead>" ;
                        str +=  "<tbody>";
                        for (var i = 0 ; i<result.smallDataSet.length ; i ++ ){
                            str += "<tr>";     
                            for (var j = 0 ; j < result.smallDataSet[i].length; j++){
                              
                                if (i == 0){
                                    str +="<td ><select style=\"width:auto;\" class=form-control m-b name=vartype id =type" + j+ " > "
                                    console.log("type data "+result.typeArr[0][0] );
//                                    console.log("boolean "+result.typeArr[0][0] == temp)
                                    for (var t = 0 ; t < result.typeArr[j].length ; t ++ ){
                                        str += "<option>" + result.typeArr[j][t].replace("double","decimal")   + "</option>";
                                    }
                                    str += "</select></td>";
                                }
                                else{
                                    str +="<td>" + result.smallDataSet[i][j] + "</td>"; 
                                }
                            }
                            str +="</tr>"; 
                        }
                        str += "</tbody>";
                    }
                    else if (datatype ==="set"){
                        str =  "<thead>" +"<tr>" ;
                        str += "<th>" + result.smallDataSet[0] + "</th>";
                        str += "</<tr>" +"</thead>" ;
                        str +=  "<tbody>";
                        for (var i = 1 ; i<result.smallDataSet.length ; i ++ ){
                            str += "<tr>";     
                            str +="<td>" + result.smallDataSet[i][0] + "</td>"; 
                            str +="</tr>"; 
                        }
                        str += "</tbody>";
                    }
                    else if(datatype === "RelSet"){//if we import other type of datasets
                        str =  "<thead>" +"<tr>" ;
                        for (var i = 0 ; i<result.columnNames.length ; i ++ ){
                            str += "<th style = \"white-space:nowrap;\">" + result.columnNames[i] + " <input  type=\"checkbox\" id=check" + i +" class=\"i-checks\" checked=\"\" ></th>";
                        }
                        str += "</tr>" +"</thead>" ;
                        str +=  "<tbody>";
                        for (var i = 0 ; i<result.smallDataSet.length ; i ++ ){
                            str += "<tr>";     
                            for (var j = 0 ; j < result.smallDataSet[i].length; j++){
                                if (i == 0){
                                    str +="<td ><select style=\"width:auto;\" class=form-control m-b name=vartype id =type" + j+ " > "
                                    for (var t = 0 ; t < result.typeArr[j].length ; t ++ ){
                                        str += "<option>" + result.typeArr[j][t].replace("double","decimal")  + "</option>";
                                    }
                                    str += "</select></td>";
                                }
                                else{
                                    str +="<td>" + result.smallDataSet[i][j] + "</td>"; 
                                }
                            }
                            str +="</tr>"; 
                        }
                        str += "</tbody>";
                    }
                    else{
                        alert("Parsing Problem in Dataset. Unknown type of dataset");
                    }
                    $("#spinner").hide();
                    if(datatype === "dicomfile"){
                        document.getElementById("dicomdataset").innerHTML = str;
                    }
                    else{
                        document.getElementById("smalldataset").innerHTML = str;
                    }
                }
                else{
                    $("#spinner").hide();
                    alert("Parsing Problem in Dataset." + result);
                    restart();
                    window.location = "index.html";
                }

                $('.i-checks').iCheck({
                    checkboxClass: 'icheckbox_square-green',
                    radioClass: 'iradio_square-green',
                });
                
                //console.log("str = " + str);
                
            },
            error : function(xhr, status, error){
                /*window.location = "http://localhost:8084/mavenproject1";*/
                $("#spinner").hide();
                if(xhr.hasOwnProperty('responseText')){
                    console.log(xhr.responseText);
                    console.log(status.toString());
                    errorHandling(xhr.responseText);
                    try{
                        var json = JSON.parse(xhr.responseText);
                        alert("Problem with loading the anonymized dataset \n"+json.error);
                    }catch(Exception){
                        alert("Parsing Problem in Dataset.\n"+error.toString());
                    }
                }
                else{
                    alert("Parsing Problem in Dataset.");
                }
                
                window.location = "index.html";
                
            }
        });  
    }  
}


//if dataset and hierarchies exists
function datahierexists(){
     $.ajax({
        url: "/action/getdashboard",
        type: "POST",
        success : function(result) {
            var str;

            if( result == "null"){
                //<input type=\"file\" name=\"buttondest\" id=\"buttondest\" style=\"display: none;\" onchange='uploadfile(\"datafile\");' webkitdirectory directory multiple mozdirectory/>
//                str = "<input type=\"file\" name=\"file\" id=\"datafile\" style=\"display: none;\" onchange='uploadfile(\"datafile\");' /><button type=\"button\" class=\"btn  btn-warning\" onclick=\"document.getElementById('datafile').click();\"> Choose Dataset<i class=\"fa fa-upload\" style=\"margin-left: 3%;\"></i></button>";
                str = "<button type=\"button\" data-target=\"#destpopup\" data-toggle=\"modal\" class=\"btn  btn-warning\"> Upload sensitive data<i class=\"fa fa-upload\" style=\"margin-left: 3%;\"></i></button>";
            }
            else if( result == "data"){
                var dropzone =  document.getElementById("my-awesome-dropzone");
                
                str = "<h3 class=\"btn btn-success\"> Data have been loaded </h3>";
                if ( dropzone != null){
                    dropzone.style.display="none";
                }
                
                str = str + "<h3><input type=\"file\" name=\"file\" id=\hierarchy\" style=\"display: none;\" onchange='uploadfile(\"hierarchy\");' /><button type=\"button\" class=\"btn btn-warning\" value=\"Load\" onclick=\"document.getElementById('hierarchy').click();\"> Load Hierarchy <i class=\"fa fa-upload\"></i></button></h3>";
            }
            else if ( result == "data_hier"){
                var dropzone =  document.getElementById("my-awesome-dropzone");

                if ( dropzone != null){
                    dropzone.style.display="none";
                }
               
                str = "<h3 class=\"btn btn-success\"> Data have been loaded </h3>";
                str = str + "<h3><input type=\"file\" name=\"file\" id=\hierarchy\" style=\"display: none;\" onchange='uploadfile(\"hierarchy\");' /><button type=\"button\" class=\"btn  btn-success\" value=\"Load\" onclick=\"document.getElementById('hierarchy').click();\"> Load Another Hierarchy <i class=\"fa fa-upload\"></i></button></h3>";
                str = str + " <h3><button type=\"button\" class=\"btn btn-warning\" onclick=\"location.href = 'myalgorithms.html'\">Proceed to Algorithms <i class=\"fa fa-arrow-circle-o-right\"></i></button></h3>";
            }
            else if (result == "data_hier_algo"){
                var dropzone =  document.getElementById("my-awesome-dropzone");

                if ( dropzone != null){
                    dropzone.style.display="none";
                }
                str = "<h3 class=\"btn btn-success\"> Data have been loaded </h3>";
                str = str + "<h3><input type=\"file\" name=\"file\" id=\hierarchy\" style=\"display: none;\" onchange='uploadfile(\"hierarchy\");' /><button type=\"button\" class=\"btn  btn-success\" value=\"Load\" onclick=\"document.getElementById('hierarchy').click();\"> Load Another Hierarchy <i class=\"fa fa-upload\"></i></button></h3>";
                str = str + " <h3><button type=\"button\" class=\"btn btn-success\" onclick=\"location.href = 'myalgorithms.html'\">Proceed to Algorithms <i class=\"fa fa-arrow-circle-o-right\"></i></button></h3>";
                str = str + " <h3><button type=\"button\" class=\"btn btn-warning\" onclick=\"location.href = 'mysolutiongraph.html'\">Proceed to Solution Graph <i class=\"fa fa-arrow-circle-o-right\"></i></button></h3>";
            }
            else if ( result == "data_hier_algo_solution"){
                var dropzone =  document.getElementById("my-awesome-dropzone");

                if ( dropzone != null){
                    dropzone.style.display="none";
                }
                
                str = "<h3 class=\"btn btn-success\"> Data have been loaded </h3>";
                str = str + "<h3><input type=\"file\" name=\"file\" id=\hierarchy\" style=\"display: none;\" onchange='uploadfile(\"hierarchy\");' /><button type=\"button\" class=\"btn  btn-success\" value=\"Load\" onclick=\"document.getElementById('hierarchy').click();\"> Load Another Hierarchy <i class=\"fa fa-upload\"></i></button></h3>";
                str = str + " <h3><button type=\"button\" class=\"btn btn-success\" onclick=\"location.href = 'myalgorithms.html'\">Proceed to Algorithms <i class=\"fa fa-arrow-circle-o-right\"></i></button></h3>";
                str = str + " <h3><button type=\"button\" class=\"btn btn-success\" onclick=\"location.href = 'mysolutiongraph.html'\">Proceed to Solution Graph <i class=\"fa fa-arrow-circle-o-right\"></i></button></h3>";
                str = str + " <h3><button type=\"button\" class=\"btn btn-warning\" onclick=\"location.href = 'myresults.html'\">Proceed to Results <i class=\"fa fa-arrow-circle-o-right\"></i></button></h3>"; 
            }

            document.getElementById("messagedashboard").innerHTML = str;
            
        },
        error : function(result){

            
            alert("Problem with loading dashboard");
        }
    }); 
}


function getStringDataset(){
    $.ajax({
        url: "/action/getattrtypes",
        type: "POST",
        success : function(result) {
            console.log(result);
            if(result!=null){
                var dropzone =  document.getElementById("my-awesome-dropzone");
                
                if ( dropzone != null){
                    dropzone.style.display="none";
                }
                
                var str =  "<thead>" +"<tr>" ;
                var columnsdef;
                var pointer = 0;
                for (var i = 0 ; i<result.columnNames.length ; i ++ ){
//                    console.log("i="+i+" "+result.colNamesType[i]+" "+(result.colNamesType[i] === "string"));
                    if(result.colNamesType[i] === "string"){
                        str += "<th  style = \"white-space:nowrap;\">" + result.columnNames[i] + "</th>";

                        if ( pointer == 0 ){
                            columnsdef = "[{\"data\":\"" +result.columnNames[i]+"\"}";
                            pointer++;
                        }
                        else{
                            columnsdef += ",{\"data\":\"" +result.columnNames[i]+"\"}";  
                        }
                    }
                }
                
                columnsdef = columnsdef +"]";
                str += "</<tr>" +"</thead>" ;
                columnsdef = JSON.parse(columnsdef);
                document.getElementById("dataset").innerHTML = str;
                
                var table = $('#dataset').DataTable({
                    "processing": true,
                    "serverSide": true,
                    "ordering": false,
                    "bFilter" : false,
                    "bPaginate" : false,
                    "bInfo": false,
                    "destroy": true,
                    
                   
                    "ajax": {
                        "url": "/action/getdataset",
                        "type": "POST",
                        "data": {start:0, length: 10},
                    },
                    
                    "columns" : columnsdef
 
                });
                
                table.destroy();
                
                $('#dataset thead').on('click', 'th', function () {
                    var mdata = table.column(this).data();
                    $("#positions").empty();
                    console.log(mdata);
                    console.log("sample "+mdata[0]);
                    mdata[0] = "567";
                    console.log(mdata);
                    var n = table.column(this).index();
                    var colname = table.settings().init().columns[n].data;
                    console.log(colname);
                    
                    table.rows().every( function ( rowIdx, tableLoop, rowLoop ) {
                        var data = this.data();
//                        console.log(data);
                        data[colname] = mdata[rowIdx]; //append a string to every col #2
                        this.data(data);
                    } );
                    
                    table.draw();
                    var sample_data = mdata[1];
                    pos = '';
                    colname = colname.replace(/\s/g, '');
                    for (var i = 0; i < sample_data.length; i++) {
                        pos+='<input type="text" id="charmask'+i+'" onclick="change_char('+i+','+n+');" class="form-control input-digit required" value="'+sample_data.charAt(i)+'">'
                    }
                    $('#positions').html(pos);
                    $('#popup').modal('show');
              
//                    table.clear();
                });
            }
        },
        error : function(result){
            document.getElementById("anonymize").disabled = true;
            alert("Dataset does not exist");
            $("#spinner").hide();
        }
    });
}



function change_char(pos,colname){
    var maskchar = document.getElementById("charmask").value;
    document.getElementById("charmask").disabled = true;
    var tempmask = maskchar.replace(/\s/g, '');
    if(tempmask.length === 0){
        document.getElementById("charmask").disabled = false;
        $('#maskwarning').show();
        return;
    }
    var char = document.getElementById("charmask"+pos).value;
    console.log(char);
    if(char !== maskchar){
        document.getElementById("charmask"+pos).value = maskchar;
    }
    else{
        var table = $("#dataset").DataTable();
        var mdata = table.column(colname).data();
        console.log(mdata);
        console.log(mdata[pos]);
        document.getElementById("charmask"+pos).value = mdata[0][pos];
    }
}

function saveMask(colname,col_idx){
    if($("#masking_opt").prop('selectedIndex') === 2){
        var regexmask = $("#regexval").val();
        var maskchar = $("#charmaskregex").val();
        var regex = regexmask.replace(/\s/g, '');
        var tempmask = maskchar.replace(/\s/g, '');
        if(tempmask.length === 0){
            document.getElementById("charmask").disabled = false;
            $('#maskwarningregex').show();
            return;
        }
        if(regex.length === 0){
            $('#regexwarning').show();
            return;
        }
        else{
            $( "#spinner" ).show();
            $.ajax({
                url: "/action/saveregex",
                type: "POST",
                data: {"column":col_idx,  "regex":regex, "char":maskchar},
                success : function(result) {
                    $( "#spinner" ).hide();
                    location.reload(); 
                },
                error : function(xhr, status, error){
                    $( "#spinner" ).hide();
                    if(xhr.hasOwnProperty('responseText')){
                        console.log(xhr.responseText);
                        console.log(status.toString());
                        errorHandling(xhr.responseText);
                        alert("Problem with saving the mask\n"+error.toString());

                    }
                    else{
                        alert("Problem with saving the mask");
                    }
                }
            });
        }
    }
    else{
        colname = colname.replace(/\s/g, '');
        var maskchar = document.getElementById("charmask").value;
        var special_pos = [];
        var i = 0;
        var counter = 0;
        $('#positions > input').each(function () { 
            if ($(this).val() === maskchar){
                special_pos[i++] = counter;
            }
            counter++;
        });
       
        var mask_option; 
        
        if($("#masking_opt").prop('selectedIndex') === 0){
            mask_option = "prefix";
        }
        else{
            mask_option = "suffix";
            console.log("before "+special_pos);
            counter--;
            for(i=0; i<special_pos.length; i++){
                special_pos[i] = Math.abs(special_pos[i] - counter); 
            }
            console.log("after "+special_pos);
            
        }
    
    
        $( "#spinner" ).show();
    
        $.ajax({
            url: "/action/savemask",
            type: "POST",
            data: {"column":col_idx, "positions":JSON.stringify(special_pos), "char":maskchar, "option":mask_option},
            success : function(result) {
                $( "#spinner" ).hide();
                location.reload(); 
            },
            error : function(xhr, status, error){
                $( "#spinner" ).hide();
                if(xhr.hasOwnProperty('responseText')){
                    console.log(xhr.responseText);
                    console.log(status.toString());
                    errorHandling(xhr.responseText);
                    alert("Problem with saving the mask\n"+error.toString());

                }
                else{
                    alert("Problem with saving the mask");
                }
            }
        });
    }
    $('#maskpopup').modal('hide');
    console.log(special_pos);
}

function makeMask(colname,col_idx,sample_data){
    var table = $("#dataset").DataTable();
    colname = colname.replace(/\s/g, '');
    var mdata = table.column(col_idx).data();
    $("#positions").empty();
    console.log(mdata);
//    var n = table.column(colname).index();
    
//    var colname = table.settings().init().columns[n].data;
//    console.log(colname);
//
//    table.rows().every( function ( rowIdx, tableLoop, rowLoop ) {
//        var data = this.data();
//    //                        console.log(data);
//        data[colname] = mdata[rowIdx]; //append a string to every col #2
//        this.data(data);
//    } );

//    table.draw();
//    var sample_data = "";
//    for(var i=0; i<mdata.length; i++){
//        if(mdata[i].replace(/\s/g, '') !== ""){
//            sample_data = mdata[i];
//            break;
//        }
//    }
    
    if(sample_data === null || sample_data === ""){
        alert("The column "+colname+" has no values in the current page");
        return;
    }
//    console.log("Delimeter "+delch);
//    if(delch !== null){
//        sample_data = sample_data.split(delch)[0];
//    }
    var pos = '';
    console.log("my"+colname);
    for (var i = 0; i < sample_data.length; i++) {
        pos+='<input type="text" id="charmask'+i+'" onclick="change_char('+i+','+col_idx+');" class="form-control input-digit" value="'+sample_data.charAt(i)+'" readonly="readonly">'
    }
    $('#positions').html(pos);
    
    $("#modal-buttons").empty();
    var buttons = '<button type="button" class="btn btn-white" data-dismiss="modal">Close</button> <button type="button" class="btn btn-w-m btn-primary"   onclick="saveMask(\''+colname+'\','+col_idx+');">Save <i class="fa fa-server" style="margin-left: 2%;"></i></button>';
    $("#modal-buttons").html(buttons);
    document.getElementById('charmask').value = '';
    document.getElementById("charmask").disabled = false;
    $("#regex_mask").hide();
    $("#simple_mask").show();
    $("#masking_opt").prop("selectedIndex", 0);
    $('#maskpopup').modal('show');
}

function changeMaskOpt(choice){
    if(choice.selectedIndex === 0 || choice.selectedIndex === 1){
        $("#regex_mask").hide();
        $("#simple_mask").show();
    }
    else{
        $("#simple_mask").hide();
        $("#regex_mask").show();
    }
}


//get dataset
function getdataset() {
    
    $.ajax({
        url: "/action/getattrtypes",
        type: "POST",
        success : function(result) {
            if ( result != null){
                console.log(result);
                if(result.pseudoanonymized){
                    $('#saveDataverse').show();
                    $('#saveZenodo').show();
                    $('#saveLocal').show();
                }
                var dropzone =  document.getElementById("my-awesome-dropzone");
                
                if ( dropzone != null){
                    dropzone.style.display="none";
                }

                var str =  "<thead>" +"<tr>" ;
                var columnsdef;
                var cuurentLocation = window.location.pathname.split('/').pop();
                for (var i = 0 ; i<result.columnNames.length ; i ++ ){
                    
                    if(cuurentLocation === "mydataset.html"){
                        if((result.hasOwnProperty("biggerSample")) && result.colNamesType[i] === "string"){
                            str += "<th style = \"white-space:nowrap;\"><div class=\"mycenter\"><span style=\"display:inline-block; margin-right:2%;\">" + result.columnNames[i] + "</span> <button onclick=\"makeMask('"+result.columnNames[i]+"',"+i+",'"+result.biggerSample[i]+"')\" type=\"button\"  class=\"btn btn-primary btn-sm btn-pseudo\"  id=\"masking\">Masking</button></div></th>";
                        }
                        else{
                            str += "<th  style = \"white-space:nowrap;\"><p style=\"margin-top:5px;\">" + result.columnNames[i] + "</p></th>";
                        }
                    }
                    else{
                        str += "<th  style = \"white-space:nowrap;\"><p style=\"margin-top:5px;\">" + result.columnNames[i] + "</p></th>";
                    }
                    
                    if ( i == 0 ){
                        columnsdef = "[{\"data\":\"" +result.columnNames[i]+"\"}";
                    }
                    else if ( i == result.columnNames.length - 1){
                        columnsdef += ",{\"data\":\"" +result.columnNames[i]+"\"}";
                    }
                    else{
                        columnsdef += ",{\"data\":\"" +result.columnNames[i]+"\"}";  
                    }
                }
                
                columnsdef = columnsdef +"]";
                str += "</<tr>" +"</thead>" ;
                columnsdef = JSON.parse(columnsdef);
//                document.getElementById("datainputname").innerHTML = result.inputFile;
                document.getElementById("dataset").innerHTML = str;
                
                if(result.hasOwnProperty('dataType') && result.dataType == "disk"){
                    $("#anonrulesBtn").hide();
                }
                else if(result.hasOwnProperty('dataType') && result.dataType == "setdata"){
                    $("#masking").hide();
                }
                
                console.log("str = " + str);

                $('#dataset').DataTable({
                    "processing": true,
                    "serverSide": true,
                    "ordering": false,
                    "bFilter" : false,
                    
                   
                    "ajax": {
                        "url": "/action/getdataset",
                        "type": "POST",
                    },
                    
                    "columns" : columnsdef
 
                });
            }
            else{
                document.getElementById("gotohier").disabled = true;
                document.getElementById("checkanonymization").disabled = true;

                alert("Dataset does not exist");
                $("#spinner").hide();
            }
        },
        error : function(result){
            document.getElementById("gotohier").disabled = true;
            document.getElementById("checkanonymization").disabled = true;
            alert("Dataset does not exist");
            $("#spinner").hide();
        }
    });   
}


//Get a sample of the dataset(four rows)
function getexampledataset(){
    
    $.ajax({
        url: "/action/getexampledataset",
        type: "POST",
        success : function(result) {
            if(result[0] === "DICOM"){
                $("#form").hide();
                $("#dicomform").show();
                document.getElementById("datatype").value = "dicomfile";
                getsmalldataset();
            }
            else{
                var str =  "<thead>" +"<tr>" ;

                str += "<th>" + result[0] + "</th>";           
                str += "</<tr>" +"</thead>" ;
                str +=  "<tbody>";

                for (var i = 1 ; i<result.length ; i ++ ){
                    str += "<tr>";     
                    str +="<td>" + result[i] + "</td>"; 
                    str +="</tr>";
                }

                str += "</tbody>";
                document.getElementById("exampledataset").innerHTML = str;
            }
        },
        error : function(result){
            alert("Dataset does not exist.");
        }
    });   
}


//load dataset with specified variables types
function loaddataset(vartypes,checkColumns){
    
    $( "#spinner" ).show();
    
    if (vartypes != null ){
        
        $.ajax({
            url: "/action/loaddataset",
            type: "POST",
            data: { vartypes: vartypes,checkColumns:checkColumns },
            success : function(result) {
                $( "#spinner" ).hide();
                window.location.href ="mydataset.html";
            },
            error : function(xhr, status, error){
                if(xhr.hasOwnProperty('responseText')){
                    console.log(xhr.responseText);
                    console.log(status.toString());
                    errorHandling(xhr.responseText);
                    try{
                        var json = JSON.parse(xhr.responseText);
                        alert("Problem with loading the dataset \n"+json.error);
//                        window.location = "index.html";
                    }catch(Exception){
                        alert("Problem with loading the dataset \n"+error.toString());
                    }
//                    alert("Problem with loading  dataset \n"+error.toString());
                    
                }
                else{
                    alert("Problem with loading dataset");
                }
                $( "#spinner" ).hide();
//                restart();
            }
        }); 
    }
    else{
        window.location.href ="mydataset.html";
    }
}



function getCountries(){
    $.ajax({
        url: "/action/getdemographicinfo",
        type: "POST",
        success : function(result) {
            if(result!=null){
                var str = "<div class=\"col-lg-2\"><label style=\"display: inline-block;\" class=\"control-label\">Countries</label></div><div class=\"col-lg-10\">";
                str += "<select id=\"countries-age\" class=\"form-control\">"; 
                var countries = result.Age;
                for(var i=0; i<countries.length; i++){
                   str += "<option value=\""+countries[i]+"\">"+countries[i]+"</option>";
                }
                str += "</select> </div>";
                document.getElementById("demographic-age").innerHTML = str;
                
                
                str = "<div class=\"col-lg-2\"><label style=\"display: inline-block;\" class=\"control-label\">Countries</label></div><div class=\"col-lg-10\">";
                str += "<select id=\"countries-zip\" class=\"form-control\">"; 
                countries = result.ZipCode;
                for(var i=0; i<countries.length; i++){
                   str += "<option value=\""+countries[i]+"\">"+countries[i]+"</option>";
                }
                str += "</select> </div>";
                document.getElementById("demographic-zip").innerHTML = str;
                $("#demogrpopup").modal('show');
               
            }
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with generating demographic hierarchy\n"+error.toString());
                
            }
            else{
                alert("Problem with generating demographic hierarchy\n");
            }
        }
    }); 
}

function generateDemHier(){
    var selectedcountry;
    var type,hier = document.getElementById("hierType").value;
    
    if(hier==="age"){
        selectedcountry = document.getElementById("countries-age").value;
        type = document.getElementById("age-type").value;
    }
    else{
        selectedcountry = document.getElementById("countries-zip").value;
        type = "string";
    }
    
    
    console.log("selected country "+selectedcountry);
    $.ajax({
        url: "/action/generatedemographichierarchy",
        type: "POST",
        data: {hier:hier, country: selectedcountry, nodeType:type},
        success : function(result) {
                if(result === "OK"){
                    window.location.href ="myhier.html";
                }
                else{
                    alert("Problem with generating demographic hierarchy for "+selectedcountry+"\n");
                }
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                try{
                    var json = JSON.parse(xhr.responseText);
                    alert("Problem with generating demographic hierarchy for "+selectedcountry+"\n"+json.error);
                    window.location = "index.html";
                }catch(Exception){
                    alert("Problem with generating demographic hierarchy for "+selectedcountry+"\n"+error.toString());
                }

            }
            else{
                alert("Problem with generating demographic hierarchy for "+selectedcountry);
            }
        }
    }); 
}

//get hierarchy graph
function gethiergraph(hierName){
    
//    console.log("edwwwwwwwwwwwwwwwwwwwww");
    $.ajax({
        url: "/action/gethiergraph",
        type: "POST",
        data: {hiername:hierName,node:"null",level:0},
        success : function(result) {
            var options = {};
            var nodes = new vis.DataSet(result.nodeList);
            var edges = new vis.DataSet(result.edgeList);
            var action;
            var container = document.getElementById('hiergraph');
            
            var data = {
                nodes: nodes,
                edges: edges,
            };
            console.log("type "+result.type);
            if(result.type.includes("demographic")){
                $("#saveHier").hide();
                options = {
                    layout:{ 
                        //randomSeed: undefined,
                        //improvedLayout:true,
                        hierarchical: {
                            enabled:true,
                            direction: "UD",
                            sortMethod:"directed"
                        }     
                    },
                    physics: {
                        enabled: false
                    },
                    interaction: {
                        navigationButtons: true,
                        hover:false
                    },
                    manipulation: {
                        enabled: false,
                        addNode: function (data, callback) {
                            action = "add";

                            document.getElementById('operation').innerHTML = "Add Node";

                            document.getElementById('node-label').value = data.label;
                            data.level = 0;
                            document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
                            document.getElementById('cancelButton').onclick = clearPopUp.bind();
                            document.getElementById('network-popUp').style.display = 'block';
                        },
                        deleteNode: function (data, callback) {
                            var delnode = [];

                            delnode = network.getSelectedNodes();

                            var delchilds = edges.get({
                                filter: function (item) {
                                    return (item.from == delnode[0]);
                                }
                            });

                            var deleNodeLabel = nodes.get(delnode[0]).label;

                            $.ajax({
                                url: "/action/deletenodehier",
                                type: "POST",
                                data: {deletenode:deleNodeLabel,hiername:hierName},
                                success : function(result) {
                                    if(result.includes("OK")){
                                        nodes.remove(delnode[0]);
                                        for( var i = 0; i < delchilds.length ; i ++ ){
                                            edges.remove(delchilds[i].id);
                                            nodes.remove(delchilds[i].to);  
                                        }
                                    }
                                    else{
                                        alert(result);
                                    }
                                },
                                error : function(xhr, status, error){
                                    if(xhr.hasOwnProperty('responseText')){
                                        console.log(xhr.responseText);
                                        console.log(status.toString());
                                        errorHandling(xhr.responseText);
                                        alert("Problem with deleting a node\n"+error.toString());

                                    }
                                    else{
                                        alert("Problem with deleting a node");
                                    }
                                }
                            });
                        },
                        editNode: function (data, callback) {
                            action ="edit";
    //                        if (data.level === 0 && data.label.indexOf('-') == -1){
    //                            alert("In distinct hierarchies, it is forbidden to process the root node");
    //                            //return; 
    //                            window.location.reload();
    //                        }
    //                        else{
    //                            document.getElementById('operation').innerHTML = "Edit Node";
    //                            document.getElementById('node-label').value = data.label;
    //                            document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
    //                            document.getElementById('cancelButton').onclick = cancelEdit.bind(this,callback);
    //                            document.getElementById('network-popUp').style.display = 'block';
    //                        }

                            document.getElementById('operation').innerHTML = "Edit Node";
                            document.getElementById('node-label').value = data.label;
                            document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
                            document.getElementById('cancelButton').onclick = cancelEdit.bind(this,callback);
                            document.getElementById('network-popUp').style.display = 'block';
                        },
                        addEdge: function (data, callback) {

                            var fromChild = edges.get({
                                filter: function (item) {
                                    return (item.from == data.from);
                                }
                            });

                            var toChild = edges.get({
                                filter: function (item) {
                                    return (item.to == data.to);
                                }
                            });

                            var fromNode = nodes.get(data.from);
                            var toNode = nodes.get(data.to);

                            console.log("eimai edwwww fromNode.label =" + fromNode.label);
                            console.log("eimai edwwwwdata.froml =" + data.from);

                            var from;
                            var to;


                            if(fromNode.label.indexOf('-') != -1){
                                from = fromNode.label.split("-");
                                to = toNode.label.split("-");
                                /*for ( var i = 0 ; i < from.length ; i = i +1 ){
                                    console.log(from[i]);
                                }*/

                                var toStart ;
                                var toEnd ;
                                var fromStart;
                                var fromEnd ;

                                if(!(to[0].toLowerCase().match(/[a-z]/i)) && !(to[1].toLowerCase().match(/[a-z]/i))){

                                    if(to[0].includes("/") || from[0].includes("/")){
                                        toStart = new Date(convertDate(to[0],true)).getTime();
    //                                    if(isNaN(toStart)){
    //                                        toStart = new Date(convertDate(to[0],true)).getTime();
    //                                    }

                                        toEnd = new Date(convertDate(to[1],false)).getTime();
                                        if(isNaN(toEnd)){
                                            toEnd = new Date(convertDate(to[1],false)).getTime();
                                        }


                                        fromStart = new Date(convertDate(from[0],true)).getTime();
                                        if(isNaN(fromStart)){
                                            fromStart = new Date(convertDate(from[0],true)).getTime();
                                        }

                                        fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        if(isNaN(fromEnd)){
                                            fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        }
                                    }
                                    else{
                                        toStart = parseInt(to[0]);
                                        toEnd = parseInt(to[1]);
                                        fromStart = parseInt(from[0]);
                                        fromEnd = parseInt(from[1]);
                                    }

                                }
                                else if(toNode.label == "(null)" || toChild== "(null)"){
                                    if(from[0].includes("/")){
                                        fromStart = new Date(convertDate(from[0],true)).getTime();
                                        if(isNaN(fromStart)){
                                            fromStart = new Date(convertDate(from[0],true)).getTime();
                                        }

                                        fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        if(isNaN(fromEnd)){
                                            fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        }
                                    }
                                    else{
                                        fromStart = parseInt(from[0]);
                                        fromEnd = parseInt(from[1]);
                                    }
                                }
                                else{
                                    alert("No letters in range types");
                                    return;
                                }
                                console.log("to1 = "+toEnd+" > from1 = " +fromEnd+"\tto0 = "+toStart+" < from0 = " +fromStart);
                                if (toEnd > fromEnd || toStart < fromStart){
                                    alert("Please, adjust new node's bounds to those of the parent");
                                    return;
                                }

                            }

                            if (toChild == "" || toChild== "(null)"){
                                nodes.update({id: data.to, text: toNode,level:fromNode.level+1});
                                callback(data);
                                $.ajax({
                                    url: "/action/addnodehier",
                                    type: "POST",
                                    data: { newnode:toNode.label , parent: fromNode.label ,hiername:hierName },
                                    success : function(result) {
                                        if(result === 'ok'){
                                            $.ajax({
                                                url: "/action/gethiergraph",
                                                type: "POST",
                                                data: {hiername:hierName,node:fromNode.label,level:fromNode.level},
                                                success : function(result) {
                                                    console.log("Ola komple meta add");
                                                    nodes.clear();
                                                    edges.clear();
                                                    nodes.add(result.nodeList);
                                                    edges.add(result.edgeList);
                                                },
                                                error : function(result){
                                                    alert("Problem with loading hierarchy. Something wrong happened when the new node was added");
                                                }
                                            });
                                        }
                                        else{
                                            alert(result);
                                            $.ajax({
                                                url: "/action/gethiergraph",
                                                type: "POST",
                                                data: {hiername:hierName,node:fromNode.label,level:fromNode.level},
                                                success : function(result) {
                                                    console.log("Ola komple meta add");
                                                    nodes.clear();
                                                    edges.clear();
                                                    nodes.add(result.nodeList);
                                                    edges.add(result.edgeList);
                                                },
                                                error : function(result){
                                                    alert("Problem with loading hierarchy. Something wrong happened when the new node was added");
                                                }
                                            });
                                        }
                                    },
                                    error : function(xhr, status, error){
                                        if(xhr.hasOwnProperty('responseText')){
                                            console.log(xhr.responseText);
                                            console.log(status.toString());
                                            errorHandling(xhr.responseText);
                                            try{
                                                var json = JSON.parse(xhr.responseText);
                                                alert("Problem with adding a new node\n"+json.error);
                                                window.location = "myhier.html";
                                            }catch(Exception){
                                                alert("Problem with adding a new node\n"+error.toString());
                                            }

                                        }
                                        else{
                                            alert("Problem with adding a new node\n");
                                        }
                                    }
                                });   
                            }
                            else{
                                alert("This move is forbidden");
                            }



                        }
                    }

                };
            }
            else{
                options = {
                    layout:{ 
                        //randomSeed: undefined,
                        //improvedLayout:true,
                        hierarchical: {
                            enabled:true,
                            direction: "UD",
                            sortMethod:"directed"
                        }     
                    },
                    physics: {
                        enabled: false
                    },
                    interaction: {
                        navigationButtons: true,
                        hover:false
                    },
                    manipulation: {
                        addNode: function (data, callback) {
                            action = "add";

                            document.getElementById('operation').innerHTML = "Add Node";

                            document.getElementById('node-label').value = data.label;
                            data.level = 0;
                            document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
                            document.getElementById('cancelButton').onclick = clearPopUp.bind();
                            document.getElementById('network-popUp').style.display = 'block';
                        },
                        deleteNode: function (data, callback) {
                            var delnode = [];

                            delnode = network.getSelectedNodes();

                            var delchilds = edges.get({
                                filter: function (item) {
                                    return (item.from == delnode[0]);
                                }
                            });

                            var deleNodeLabel = nodes.get(delnode[0]).label;

                            $.ajax({
                                url: "/action/deletenodehier",
                                type: "POST",
                                data: {deletenode:deleNodeLabel,hiername:hierName},
                                success : function(result) {
                                    if(result.includes("OK")){
                                        nodes.remove(delnode[0]);
                                        for( var i = 0; i < delchilds.length ; i ++ ){
                                            edges.remove(delchilds[i].id);
                                            nodes.remove(delchilds[i].to);  
                                        }
                                    }
                                    else{
                                        alert(result);
                                    }
                                },
                                error : function(xhr, status, error){
                                    if(xhr.hasOwnProperty('responseText')){
                                        console.log(xhr.responseText);
                                        console.log(status.toString());
                                        errorHandling(xhr.responseText);
                                        alert("Problem with deleting a node\n"+error.toString());

                                    }
                                    else{
                                        alert("Problem with deleting a node");
                                    }
                                }
                            });
                        },
                        editNode: function (data, callback) {
                            action ="edit";
    //                        if (data.level === 0 && data.label.indexOf('-') == -1){
    //                            alert("In distinct hierarchies, it is forbidden to process the root node");
    //                            //return; 
    //                            window.location.reload();
    //                        }
    //                        else{
    //                            document.getElementById('operation').innerHTML = "Edit Node";
    //                            document.getElementById('node-label').value = data.label;
    //                            document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
    //                            document.getElementById('cancelButton').onclick = cancelEdit.bind(this,callback);
    //                            document.getElementById('network-popUp').style.display = 'block';
    //                        }

                            document.getElementById('operation').innerHTML = "Edit Node";
                            document.getElementById('node-label').value = data.label;
                            document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
                            document.getElementById('cancelButton').onclick = cancelEdit.bind(this,callback);
                            document.getElementById('network-popUp').style.display = 'block';
                        },
                        addEdge: function (data, callback) {

                            var fromChild = edges.get({
                                filter: function (item) {
                                    return (item.from == data.from);
                                }
                            });

                            var toChild = edges.get({
                                filter: function (item) {
                                    return (item.to == data.to);
                                }
                            });

                            var fromNode = nodes.get(data.from);
                            var toNode = nodes.get(data.to);

                            console.log("eimai edwwww fromNode.label =" + fromNode.label);
                            console.log("eimai edwwwwdata.froml =" + data.from);

                            var from;
                            var to;


                            if(fromNode.label.indexOf('-') != -1){
                                from = fromNode.label.split("-");
                                to = toNode.label.split("-");
                                /*for ( var i = 0 ; i < from.length ; i = i +1 ){
                                    console.log(from[i]);
                                }*/

                                var toStart ;
                                var toEnd ;
                                var fromStart;
                                var fromEnd ;

                                if(!(to[0].toLowerCase().match(/[a-z]/i)) && !(to[1].toLowerCase().match(/[a-z]/i))){

                                    if(to[0].includes("/") || from[0].includes("/")){
                                        toStart = new Date(convertDate(to[0],true)).getTime();
    //                                    if(isNaN(toStart)){
    //                                        toStart = new Date(convertDate(to[0],true)).getTime();
    //                                    }

                                        toEnd = new Date(convertDate(to[1],false)).getTime();
                                        if(isNaN(toEnd)){
                                            toEnd = new Date(convertDate(to[1],false)).getTime();
                                        }


                                        fromStart = new Date(convertDate(from[0],true)).getTime();
                                        if(isNaN(fromStart)){
                                            fromStart = new Date(convertDate(from[0],true)).getTime();
                                        }

                                        fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        if(isNaN(fromEnd)){
                                            fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        }
                                    }
                                    else{
                                        toStart = parseInt(to[0]);
                                        toEnd = parseInt(to[1]);
                                        fromStart = parseInt(from[0]);
                                        fromEnd = parseInt(from[1]);
                                    }

                                }
                                else if(toNode.label == "(null)" || toChild== "(null)"){
                                    if(from[0].includes("/")){
                                        fromStart = new Date(convertDate(from[0],true)).getTime();
                                        if(isNaN(fromStart)){
                                            fromStart = new Date(convertDate(from[0],true)).getTime();
                                        }

                                        fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        if(isNaN(fromEnd)){
                                            fromEnd = new Date(convertDate(from[1],false)).getTime();
                                        }
                                    }
                                    else{
                                        fromStart = parseInt(from[0]);
                                        fromEnd = parseInt(from[1]);
                                    }
                                }
                                else{
                                    alert("No letters in range types");
                                    return;
                                }
                                console.log("to1 = "+toEnd+" > from1 = " +fromEnd+"\tto0 = "+toStart+" < from0 = " +fromStart);
                                if (toEnd > fromEnd || toStart < fromStart){
                                    alert("Please, adjust new node's bounds to those of the parent");
                                    return;
                                }

                            }

                            if (toChild == "" || toChild== "(null)"){
                                nodes.update({id: data.to, text: toNode,level:fromNode.level+1});
                                callback(data);
                                $.ajax({
                                    url: "/action/addnodehier",
                                    type: "POST",
                                    data: { newnode:toNode.label , parent: fromNode.label ,hiername:hierName },
                                    success : function(result) {
                                        if(result === 'ok'){
                                            $.ajax({
                                                url: "/action/gethiergraph",
                                                type: "POST",
                                                data: {hiername:hierName,node:fromNode.label,level:fromNode.level},
                                                success : function(result) {
                                                    console.log("Ola komple meta add");
                                                    nodes.clear();
                                                    edges.clear();
                                                    nodes.add(result.nodeList);
                                                    edges.add(result.edgeList);
                                                },
                                                error : function(result){
                                                    alert("Problem with loading hierarchy. Something wrong happened when the new node was added");
                                                }
                                            });
                                        }
                                        else{
                                            alert(result);
                                            $.ajax({
                                                url: "/action/gethiergraph",
                                                type: "POST",
                                                data: {hiername:hierName,node:fromNode.label,level:fromNode.level},
                                                success : function(result) {
                                                    console.log("Ola komple meta add");
                                                    nodes.clear();
                                                    edges.clear();
                                                    nodes.add(result.nodeList);
                                                    edges.add(result.edgeList);
                                                },
                                                error : function(result){
                                                    alert("Problem with loading hierarchy. Something wrong happened when the new node was added");
                                                }
                                            });
                                        }
                                    },
                                    error : function(xhr, status, error){
                                        if(xhr.hasOwnProperty('responseText')){
                                            console.log(xhr.responseText);
                                            console.log(status.toString());
                                            errorHandling(xhr.responseText);
                                            try{
                                                var json = JSON.parse(xhr.responseText);
                                                alert("Problem with adding a new node\n"+json.error);
                                                window.location = "myhier.html";
                                            }catch(Exception){
                                                alert("Problem with adding a new node\n"+error.toString());
                                            }

                                        }
                                        else{
                                            alert("Problem with adding a new node\n");
                                        }
                                    }
                                });   
                            }
                            else{
                                alert("This move is forbidden");
                            }



                        }
                    }

                };
            }
            
            

            function leapYear(year)
            {
              return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
            }


            
            function convertDate(date,start){ // convert dd/mm/yyyy string to mm-dd-yyyy
                
                var dateParts = date.split("/");
                if(dateParts.length > 2){ // dd/mm/yyyy
                    dateParts[0] = dateParts[0].length == 1 ? "0"+dateParts[0] : dateParts[0];
                    dateParts[1] = dateParts[1].length == 1 ? "0"+dateParts[1] : dateParts[1];
                    console.log("1: "+dateParts[1]+"/"+dateParts[0]+"/"+dateParts[2]);
                    return dateParts[1]+"/"+dateParts[0]+"/"+dateParts[2];
                }
                else if(dateParts.length==1){ // yyyy
                    if(start){
                        return "01/01/"+dateParts[0];
                    }
                    else{
                        return "12/31/"+dateParts[0];
                    }
                }
                else{ // mm/yyyy
                    dateParts[0] = dateParts[0].length == 1 ? "0"+dateParts[0] : dateParts[0];
                    if(start){
                        console.log("2: "+dateParts[0]+"/01/"+dateParts[1]);
                        return dateParts[0]+"/01/"+dateParts[1];
                    }
                    else{
                        if(parseInt(dateParts[0])==2){
                            if(leapYear(dateParts[1])){
                                console.log("3: "+dateParts[0]+"/29/"+dateParts[1]);
                                return dateParts[0]+"/29/"+dateParts[1];
                            }
                            else{
                                console.log("3: "+dateParts[0]+"/28/"+dateParts[1]);
                                return dateParts[0]+"/28/"+dateParts[1];
                            }
                        }
                        else if(((parseInt(dateParts[0]) % 2) == 0) && parseInt(dateParts[0])<8){
                            console.log("3: "+dateParts[0]+"/30/"+dateParts[1]);
                            return dateParts[0]+"/30/"+dateParts[1];
                        }
                        else{
                            if(parseInt(dateParts[0]) == 9 || dateParts[0] == 11){
                                console.log("3: "+dateParts[0]+"/30/"+dateParts[1]);
                                return dateParts[0]+"/30/"+dateParts[1];
                            }
                            console.log("3: "+dateParts[0]+"/31/"+dateParts[1]);
                            return dateParts[0]+"/31/"+dateParts[1];
                        }

                    }
                }
            }
            var network = new vis.Network(container, data, options);

            function clearPopUp() {
                document.getElementById('saveButton').onclick = null;
                document.getElementById('cancelButton').onclick = null;
                document.getElementById('network-popUp').style.display = 'none';
            }

            function cancelEdit(callback) {
                clearPopUp();
                callback(null);
            }

            function saveData(data,callback) {
                //
                data.label = document.getElementById('node-label').value;
                //data.id = document.getElementById('node-label').value;
                clearPopUp();
                 var newNode = data.label;
                if(newNode.indexOf('-') != -1 && newNode.toLowerCase().match(/[a-z]/i) && newNode != "(null)"){
                    alert("No letters in range types");
                    window.location.reload();
                    return;
                }
                if ( action != "edit"){
                    var node = nodes.get({
                        filter: function (item) {
                          return (item.label == data.label);
                        }
                    });

                    if ( node == ""){
                        callback(data);
                    }
                    else{
                        confirm("This node exists in hierarchy");  
                        callback(null);
                        
                    }
                }
                else{
                    var node = nodes.get({
                        filter: function (item) {
                            return (item.label == data.label);
                        }
                    });
                   
                  
                    var editNode = nodes.get(data.id);
                    
                    if ( node == ""){
                        console.log("oldnode = " + editNode.label  + " newnode = " + data.label  + " hiername = " + hierName);
                        
                        $.ajax({
                            url: "/action/editnodehier",
                            type: "POST",
                            data: { oldnode:editNode.label , newnode: data.label ,hiername:hierName },
                            success : function(result) {
//                                window.location.reload();
                                nodes.update(editNode.label=data.label);
                                nodes.remove(editNode);
                                
                            },
                            error : function(xhr, status, error){
                                if(xhr.hasOwnProperty('responseText')){
                                    console.log(xhr.responseText);
                                    console.log(status.toString());
                                    errorHandling(xhr.responseText);
                                    alert("Problem with editing the node\n"+error.toString());
                                    
                                }
                                else{
                                    alert("Problem with editing the node\n");
                                }
                            }
                        }); 
                        callback(data);
                    }
                    else{
                        alert("This node exists in hierarchy");
                        callback(null);
                    }
                }
            }
       
            network.on('doubleClick', function (properties) {
                var node = nodes.get(properties.nodes.toString());
                
                console.log("nodeeeee = " + node.id);
                console.log("level = " + node.level);
                
                if (node == null){
                    $.ajax({
                        url: "/action/gethiergraph",
                        type: "GET",
                        data: {hiername:hierName,node:"null",level:0},
                        success : function(result) {
                            nodes.clear();
                            edges.clear();
                            nodes.add(result.nodeList);
                            edges.add(result.edgeList);
                        },
                        error : function(xhr, status, error){
                            if(xhr.hasOwnProperty('responseText')){
                                console.log(xhr.responseText);
                                console.log(status.toString());
                                errorHandling(xhr.responseText);
                                alert("Problem with loading the hierarchy\n"+error.toString());
                                
                            }
                            else{
                                alert("Problem with loading the hierarchy\n");
                            }
                        }
                    });    
                }
                else{
                    //if( !node.label == "(null)" ){
                        $.ajax({
                            url: "/action/gethiergraph",
                            type: "POST",
                            data: {hiername:hierName,node:node.label,level:node.level},
                            success : function(result) {
                                nodes.clear();
                                edges.clear();
                                nodes.add(result.nodeList);
                                edges.add(result.edgeList);
                            },
                            error : function(xhr, status, error){
                                if(xhr.hasOwnProperty('responseText')){
                                    console.log(xhr.responseText);
                                    console.log(status.toString());
                                    errorHandling(xhr.responseText);
                                    alert("Problem with loading the node's children\n"+error.toString());
                                    
                                }
                                else{
                                    alert("Problem with editing the node's children\n");
                                }
                            }
                        });
                    //}
                }
            });
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with loading the hierarchy\n"+error.toString());
                
            }
            else{
                alert("Problem with loading the hierarchy\n");
            }
        }
    }); 
}


//get names of hierarchies
function getHierNames(){
    
    $.ajax({
        url: "/action/gethierarchies",
        type: "POST",
        data:{selectedhier:"null"},
        success : function(result) {
            var str;
            var hierarchymenu = "<li style=\"position:relative;\"><a><form>Load<input  type=\"file\"  name=\"file\" id=\"hierarchy\" onchange ='uploadfile(\"hierarchy\");'style='position:absolute;z-index:2;top:0;left:0;filter: alpha(opacity=0);-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)\";opacity:0;background-color:transparent;color:transparent;' name=\"file_source\" size=\"40\"/></form></a> </li>  <li><a href=\"myhierarchywizard.html\" >Auto Generate </a></li>"

            if (result != ""){
                for ( var i = 0 ; i < result.length ; i ++){
                    if ( i == 0 ){
                        str = "<li><a href=\"#\" onclick=\"saveselectedhier('" + result[i].id + "')\" > " + result[i].id + "</a></li>";
                    }
                    else{
                       str = str + "<li><a href=\"#\" onclick=\"saveselectedhier('" + result[i].id + "')\" > " + result[i].id + "</a></li>"; 
                    }
                }
                str = str + hierarchymenu;
            }
            else{
                str = hierarchymenu;
            }
            document.getElementById("hierarchyname").innerHTML = str;
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with loading the hierarchies' names\n"+error.toString());
               
            }
            else{
                alert("Problem with loading the hierarchies' names\n");
            }
        }
    });   
}


//save selected hierarchy
function saveselectedhier(hiername){
    
    $.ajax({
        url: "/action/saveselectedhier",
        type: "POST",
        data: { hiername:hiername },
        success : function(result) {
            window.location = "myhier.html";
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with saving the hierarchy\n"+error.toString());
                
            }
            else{
                alert("Problem with saving the hierarchy\n");
            }
        }
    });  
}

function removeHierarchy(){
    $.ajax({
        url: "/action/removehierarchy",
        type: "POST",
        success : function(result){
            if(result === 'no'){
                alert("There is no hierarchy to remove");
            }
            else{
                window.location.reload();
            }
        },
        error : function(result){
            if(result === 'no'){
                alert("There is no hierarchy to remove");
            }
            else{
                alert("Problem with removing the hierarchy\n");
            }
        }
    });
}

function getDataType(){
    $.ajax({
        url: "/action/getdatatype",
        type: "POST",
        success : function(datatype) {
            if(datatype !== "tabular"){
                document.getElementById("demographicBtn").style.display = "none";
            }
        },
        error: function(xhr, status, error){
           if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Something wrong happened, please try again! \n"+error.toString());
                
            }
            else{
                alert("Something wrong happened, please try again!\n");
            }
        }
    });
}

//get hierarchy and hierarchy graph
function getHierarchies(){
    
    $.ajax({
        url: "/action/getselectedhier",
        type: "POST",
        success : function(selectedhier) {

            console.log("select hier = " + selectedhier);

            if (selectedhier === ""){
                document.getElementById("gotoalgo").disabled = true;
                document.getElementById("remove").style.display = "none";
            }
            else{
                $.ajax({
                    url: "/action/gethierarchies",
                    type: "POST",
                    data:{selectedhier:selectedhier},
                    success : function(result) {
                        
                        
                        $(".select2_demo_1").select2({data: result}).on("change", function() {
                            console.log("change = " + this.value);
                            console.log("result = " + result[0].id );
                            gethiergraph(this.value);
                        });

                        gethiergraph(result[0].id);
                        document.getElementById("remove").style.display = "inline-block";
                        
                        for(var i=0; i< result.length; i++){
                            if(!result[i].hierType.includes("demographic")){
                                document.getElementById("demographicBtn").style.display = "none";
                            }
                            else{
                                document.getElementById("loadhierBtn").style.display = "none";
                                document.getElementById("autogenerateBtn").style.display = "none";
                            }
                        }
                        
                    },
                    error : function(xhr, status, error){
                        if(xhr.hasOwnProperty('responseText')){
                            console.log(xhr.responseText);
                            console.log(status.toString());
                            errorHandling(xhr.responseText);
                            alert("Problem with loading the hierarchy "+selectedhier+"\n"+error.toString());
                            
                        }
                        else{
                            alert("Problem with loading the hierarchy "+selectedhier+"\n");
                        }
                    }
                });
            }
        },
        error : function(xhr, status, error){
           if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with loading the availables hierarchies \n"+error.toString());
                
            }
            else{
                alert("Problem with loading the availables hierarchies \n");
            }
        }
    });  
}


//get bindings between hierarchies and quasi-identifiers
function getrelations(){
    
    $.ajax({
        url: "/action/getattrtypes",
        type: "POST",
        success : function(attributes) {
            if ( attributes != null ){
                
                $.ajax({
                    url: "/action/gethierarchies",
                    type: "POST",
                    data:{selectedhier:"null"},
                    success : function(hierarchies) {
                        var str;
                    
                        for ( var i = 0 ; i < attributes.columnNames.length ; i ++){
                            if (i == 0){
                                str = "<div class=\"form-group\"><label class=\"col-lg-2 control-label\">" + attributes.columnNames[i] + "</label><div class=\"col-lg-10\">";                           
                                str += "<select class=\"relation"+ i + " form-control\">"; 
                                str += "<option </option>";
                                for ( var j = 0 ; j < hierarchies.length ; j ++){
                                    if (hierarchies[j].type === attributes.colNamesType[i]){
                                        str += "<option value=\"" + hierarchies[j].id +"\">" + hierarchies[j].id + "</option>";
                                    }
                                    else if(attributes.colNamesType[i] === "set" && hierarchies[j].type=== "string"){
                                        str += "<option value=\"" + hierarchies[j].id +"\">" + hierarchies[j].id + "</option>";
                                    }
                                }
                                str += "</select> </div> </div>";
                            }
                            else{
                                str += "<div class=\"form-group\"><label class=\"col-lg-2 control-label\">" + attributes.columnNames[i] + "</label><div class=\"col-lg-10\">";
                                str += "<select class=\"relation"+ i + " form-control\">"; 
                                str += "<option </option>";
                                for ( var j = 0 ; j < hierarchies.length ; j ++){
                                    if (hierarchies[j].type === attributes.colNamesType[i]){
                                        str += "<option value=\"" + hierarchies[j].id +"\">" + hierarchies[j].id + "</option>";
                                    }
                                    else if(attributes.colNamesType[i] === "set" && hierarchies[j].type === "string"){
                                        str += "<option value=\"" + hierarchies[j].id +"\">" + hierarchies[j].id + "</option>";
                                    }
                                }
                                str += "</select> </div> </div>";
                            }
                        }
                   
                        document.getElementById("relations").innerHTML = str;
                    
                        relations.addEventListener("click", function() {
                            var FLAG = "false";
                            for (var index = 0; index < relations.length; index++) {
                                if ( $(".relation" +index).val() != "" ){
                                    FLAG ="true";
                                }
                            }
                            if (FLAG == "false"){
                                $('#algovalid').css('color', 'red');
                                $('#relations').css('color', 'red');
                            }
                            else{
                                $('#algovalid').css('color', 'grey');
                                $('#relations').css('color', 'grey');
                            }
                        });
                    },
                    error : function(xhr, status, error){
                        if(xhr.hasOwnProperty('responseText')){
                            console.log(xhr.responseText);
                            console.log(status.toString());
                            errorHandling(xhr.responseText);
                            alert("Problem with loading attributes of dataset\n"+error.toString());
                            
                        }
                        else{
                            alert("Problem with loading attributes of dataset\n");
                        }
                    }
                });
            }   
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with loading hierarchies' names\n"+error.toString());
                
            }
            else{
                alert("Problem with loading hierarchies' names\n");
            }
        }
    });
}


//algorithm execution
function algorithmExecution(){

    var k = $( ".kchoice" ).val();
    var algo = $(".algorithm").val();
    var m = 0;
    
    if (algo == "apriori" || algo=="mixedapriori"){
        m = $( ".mchoice" ).val();
    }

    var FLAG = "false";
    var relations;
    for ( var i = 0 ; i < document.getElementById("relations").length ; i ++){
        if ( i == 0 ){
            if ($(".relation" +i).val() != ""){
                FLAG = "true";
            }
            relations = $(".relation" +i).val();
        }
        else{
            if ($(".relation" +i).val() != ""){
                FLAG = "true";
            }
            relations = relations + "," + $(".relation" +i).val();
        }
    }
    var colSet =  document.getElementById("setcolumn").value;
    if(colSet != "" && $(".relation" +colSet.split("_")[1]).val() == ""){
        alert("You must bind set-valued column with a hierarchy");
        return;
    }
    if (FLAG == "false"){
        $('#algovalid').css('color', 'red');
        alert("Bind hierarchies with attributes");
    }
    else{
        $( "#spinner" ).show();
        $.ajax({
            url: "/action/algorithmexecution",
            type: "POST",
            data: { k: k,m:m, algo:algo,relations:relations },
            success : function(result) {
                $( "#spinner" ).hide();
                
                console.log("result == " + result);
                
                if ( result == "memory problem"){
                    alert("Not enough memory space.");
                    restart();
                }
                else if( result == "noresults"){
                    alert("Dataset is already anonymized for k : " + k);
                }
                else if( result == "outoftime"){
                    alert("The algorithm execution takes more than 3 minutes, please download the desktop version, the online version is only for simple execution.");
                    restart();
                }
                else if (result.toLowerCase().includes("hierarchy")){
                    alert(result);
                }
                else if( result == "wrong"){
                    alert("Something wrong happened while the algorithm was executing");
                }
                else{
                    if (algo == "apriori" || algo == "mixedapriori" || algo == "clustering" || algo === "demographic" || algo === "dp"){
                       window.location = "myresults.html";
                    }
                    else{
                        window.location = "mysolutiongraph.html";
                    }
                }
            },
            error : function(xhr, status, error){
                $( "#spinner" ).hide();
                if(xhr.hasOwnProperty('responseText')){
                    console.log(xhr.responseText);
                    console.log(status.toString());
                    errorHandling(xhr.responseText);
                    alert("Something wrong happened while the algorithm was executing \n"+error.toString());
                    
                }
                else{
                    alert("Something wrong happened while the algorithm was executing");
                }
            }
        });  
    }
}
    
    
//get solution graph 
function getsolutiongraph(){
    console.log("solutions");
    $.ajax({
        url: "/action/getsolutiongraph",
        type: "GET",
        success : function(result) {
            var nodes = new vis.DataSet(result.nodeList);
            var edges = new vis.DataSet(result.edgeList);
            var container = document.getElementById('solgraph');
            
            //var nodes =[{"label":"[0, 0, 0]","level":0,"id":"[0, 0, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[1, 0, 0]","level":1,"id":"[1, 0, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[0, 1, 0]","level":1,"id":"[0, 1, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[0, 0, 1]","level":1,"id":"[0, 0, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[2, 0, 0]","level":2,"id":"[2, 0, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[1, 1, 0]","level":2,"id":"[1, 1, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[1, 0, 1]","level":2,"id":"[1, 0, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[0, 2, 0]","level":2,"id":"[0, 2, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[0, 1, 1]","level":2,"id":"[0, 1, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[0, 0, 2]","level":2,"id":"[0, 0, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[3, 0, 0]","level":3,"id":"[3, 0, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[2, 1, 0]","level":3,"id":"[2, 1, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[2, 0, 1]","level":3,"id":"[2, 0, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[1, 2, 0]","level":3,"id":"[1, 2, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[1, 1, 1]","level":3,"id":"[1, 1, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[1, 0, 2]","level":3,"id":"[1, 0, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[0, 3, 0]","level":3,"id":"[0, 3, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[0, 2, 1]","level":3,"id":"[0, 2, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[0, 1, 2]","level":3,"id":"[0, 1, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[0, 0, 3]","level":3,"id":"[0, 0, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[3, 1, 0]","level":4,"id":"[3, 1, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[3, 0, 1]","level":4,"id":"[3, 0, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[2, 2, 0]","level":4,"id":"[2, 2, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[2, 1, 1]","level":4,"id":"[2, 1, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[2, 0, 2]","level":4,"id":"[2, 0, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[1, 3, 0]","level":4,"id":"[1, 3, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[1, 2, 1]","level":4,"id":"[1, 2, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[1, 1, 2]","level":4,"id":"[1, 1, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[1, 0, 3]","level":4,"id":"[1, 0, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[0, 3, 1]","level":4,"id":"[0, 3, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[0, 2, 2]","level":4,"id":"[0, 2, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[0, 1, 3]","level":4,"id":"[0, 1, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[3, 2, 0]","level":5,"id":"[3, 2, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[3, 1, 1]","level":5,"id":"[3, 1, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[3, 0, 2]","level":5,"id":"[3, 0, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[2, 3, 0]","level":5,"id":"[2, 3, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[2, 2, 1]","level":5,"id":"[2, 2, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[2, 1, 2]","level":5,"id":"[2, 1, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[2, 0, 3]","level":5,"id":"[2, 0, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[1, 3, 1]","level":5,"id":"[1, 3, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[1, 2, 2]","level":5,"id":"[1, 2, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[1, 1, 3]","level":5,"id":"[1, 1, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[0, 3, 2]","level":5,"id":"[0, 3, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[0, 2, 3]","level":5,"id":"[0, 2, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[3, 3, 0]","level":6,"id":"[3, 3, 0]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 0\n"},{"label":"[3, 2, 1]","level":6,"id":"[3, 2, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[3, 1, 2]","level":6,"id":"[3, 1, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[3, 0, 3]","level":6,"id":"[3, 0, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 0\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[2, 3, 1]","level":6,"id":"[2, 3, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[2, 2, 2]","level":6,"id":"[2, 2, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[2, 1, 3]","level":6,"id":"[2, 1, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[1, 3, 2]","level":6,"id":"[1, 3, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[1, 2, 3]","level":6,"id":"[1, 2, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[0, 3, 3]","level":6,"id":"[0, 3, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 0\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[3, 3, 1]","level":7,"id":"[3, 3, 1]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 1\n"},{"label":"[3, 2, 2]","level":7,"id":"[3, 2, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[3, 1, 3]","level":7,"id":"[3, 1, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 1\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[2, 3, 2]","level":7,"id":"[2, 3, 2]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[2, 2, 3]","level":7,"id":"[2, 2, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[1, 3, 3]","level":7,"id":"[1, 3, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 1\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[3, 3, 2]","level":8,"id":"[3, 3, 2]","color":"lightblue","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 2\n"},{"label":"[3, 2, 3]","level":8,"id":"[3, 2, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 2\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[2, 3, 3]","level":8,"id":"[2, 3, 3]","color":"red","title":"Quasi Identifier : \"postcode\" generalized to level 2\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 3\n"},{"label":"[3, 3, 3]","level":9,"id":"[3, 3, 3]","color":"lightblue","title":"Quasi Identifier : \"postcode\" generalized to level 3\nQuasi Identifier : \"date\" generalized to level 3\nQuasi Identifier : \"age\" generalized to level 3\n"}];
            //var edges = [{"from":"[0, 0, 0]","to":"[0, 1, 0]"},{"from":"[0, 0, 0]","to":"[1, 0, 0]"},{"from":"[0, 0, 0]","to":"[0, 0, 1]"},{"from":"[1, 0, 0]","to":"[2, 0, 0]"},{"from":"[1, 0, 0]","to":"[1, 1, 0]"},{"from":"[1, 0, 0]","to":"[1, 0, 1]"},{"from":"[0, 1, 0]","to":"[1, 1, 0]"},{"from":"[0, 1, 0]","to":"[0, 2, 0]"},{"from":"[0, 1, 0]","to":"[0, 1, 1]"},{"from":"[0, 0, 1]","to":"[1, 0, 1]"},{"from":"[0, 0, 1]","to":"[0, 1, 1]"},{"from":"[0, 0, 1]","to":"[0, 0, 2]"},{"from":"[2, 0, 0]","to":"[3, 0, 0]"},{"from":"[2, 0, 0]","to":"[2, 1, 0]"},{"from":"[2, 0, 0]","to":"[2, 0, 1]"},{"from":"[1, 1, 0]","to":"[2, 1, 0]"},{"from":"[1, 1, 0]","to":"[1, 2, 0]"},{"from":"[1, 1, 0]","to":"[1, 1, 1]"},{"from":"[1, 0, 1]","to":"[2, 0, 1]"},{"from":"[1, 0, 1]","to":"[1, 1, 1]"},{"from":"[1, 0, 1]","to":"[1, 0, 2]"},{"from":"[0, 2, 0]","to":"[1, 2, 0]"},{"from":"[0, 2, 0]","to":"[0, 3, 0]"},{"from":"[0, 2, 0]","to":"[0, 2, 1]"},{"from":"[0, 1, 1]","to":"[1, 1, 1]"},{"from":"[0, 1, 1]","to":"[0, 2, 1]"},{"from":"[0, 1, 1]","to":"[0, 1, 2]"},{"from":"[0, 0, 2]","to":"[0, 0, 3]"},{"from":"[0, 0, 2]","to":"[0, 1, 2]"},{"from":"[0, 0, 2]","to":"[1, 0, 2]"},{"from":"[3, 0, 0]","to":"[3, 1, 0]"},{"from":"[3, 0, 0]","to":"[3, 0, 1]"},{"from":"[2, 1, 0]","to":"[2, 2, 0]"},{"from":"[2, 1, 0]","to":"[3, 1, 0]"},{"from":"[2, 1, 0]","to":"[2, 1, 1]"},{"from":"[2, 0, 1]","to":"[3, 0, 1]"},{"from":"[2, 0, 1]","to":"[2, 1, 1]"},{"from":"[2, 0, 1]","to":"[2, 0, 2]"},{"from":"[1, 2, 0]","to":"[2, 2, 0]"},{"from":"[1, 2, 0]","to":"[1, 3, 0]"},{"from":"[1, 2, 0]","to":"[1, 2, 1]"},{"from":"[1, 1, 1]","to":"[2, 1, 1]"},{"from":"[1, 1, 1]","to":"[1, 2, 1]"},{"from":"[1, 1, 1]","to":"[1, 1, 2]"},{"from":"[1, 0, 2]","to":"[2, 0, 2]"},{"from":"[1, 0, 2]","to":"[1, 1, 2]"},{"from":"[1, 0, 2]","to":"[1, 0, 3]"},{"from":"[0, 3, 0]","to":"[1, 3, 0]"},{"from":"[0, 3, 0]","to":"[0, 3, 1]"},{"from":"[0, 2, 1]","to":"[1, 2, 1]"},{"from":"[0, 2, 1]","to":"[0, 3, 1]"},{"from":"[0, 2, 1]","to":"[0, 2, 2]"},{"from":"[0, 1, 2]","to":"[1, 1, 2]"},{"from":"[0, 1, 2]","to":"[0, 2, 2]"},{"from":"[0, 1, 2]","to":"[0, 1, 3]"},{"from":"[0, 0, 3]","to":"[0, 1, 3]"},{"from":"[0, 0, 3]","to":"[1, 0, 3]"},{"from":"[3, 1, 0]","to":"[3, 2, 0]"},{"from":"[3, 1, 0]","to":"[3, 1, 1]"},{"from":"[3, 0, 1]","to":"[3, 1, 1]"},{"from":"[3, 0, 1]","to":"[3, 0, 2]"},{"from":"[2, 2, 0]","to":"[2, 3, 0]"},{"from":"[2, 2, 0]","to":"[3, 2, 0]"},{"from":"[2, 2, 0]","to":"[2, 2, 1]"},{"from":"[2, 1, 1]","to":"[3, 1, 1]"},{"from":"[2, 1, 1]","to":"[2, 2, 1]"},{"from":"[2, 1, 1]","to":"[2, 1, 2]"},{"from":"[2, 0, 2]","to":"[3, 0, 2]"},{"from":"[2, 0, 2]","to":"[2, 1, 2]"},{"from":"[2, 0, 2]","to":"[2, 0, 3]"},{"from":"[1, 3, 0]","to":"[2, 3, 0]"},{"from":"[1, 3, 0]","to":"[1, 3, 1]"},{"from":"[1, 2, 1]","to":"[2, 2, 1]"},{"from":"[1, 2, 1]","to":"[1, 3, 1]"},{"from":"[1, 2, 1]","to":"[1, 2, 2]"},{"from":"[1, 1, 2]","to":"[2, 1, 2]"},{"from":"[1, 1, 2]","to":"[1, 2, 2]"},{"from":"[1, 1, 2]","to":"[1, 1, 3]"},{"from":"[1, 0, 3]","to":"[2, 0, 3]"},{"from":"[1, 0, 3]","to":"[1, 1, 3]"},{"from":"[0, 3, 1]","to":"[1, 3, 1]"},{"from":"[0, 3, 1]","to":"[0, 3, 2]"},{"from":"[0, 2, 2]","to":"[1, 2, 2]"},{"from":"[0, 2, 2]","to":"[0, 3, 2]"},{"from":"[0, 2, 2]","to":"[0, 2, 3]"},{"from":"[0, 1, 3]","to":"[1, 1, 3]"},{"from":"[0, 1, 3]","to":"[0, 2, 3]"},{"from":"[3, 2, 0]","to":"[3, 3, 0]"},{"from":"[3, 2, 0]","to":"[3, 2, 1]"},{"from":"[3, 1, 1]","to":"[3, 2, 1]"},{"from":"[3, 1, 1]","to":"[3, 1, 2]"},{"from":"[3, 0, 2]","to":"[3, 1, 2]"},{"from":"[3, 0, 2]","to":"[3, 0, 3]"},{"from":"[2, 3, 0]","to":"[3, 3, 0]"},{"from":"[2, 3, 0]","to":"[2, 3, 1]"},{"from":"[2, 2, 1]","to":"[3, 2, 1]"},{"from":"[2, 2, 1]","to":"[2, 3, 1]"},{"from":"[2, 2, 1]","to":"[2, 2, 2]"},{"from":"[2, 1, 2]","to":"[3, 1, 2]"},{"from":"[2, 1, 2]","to":"[2, 2, 2]"},{"from":"[2, 1, 2]","to":"[2, 1, 3]"},{"from":"[2, 0, 3]","to":"[3, 0, 3]"},{"from":"[2, 0, 3]","to":"[2, 1, 3]"},{"from":"[1, 3, 1]","to":"[2, 3, 1]"},{"from":"[1, 3, 1]","to":"[1, 3, 2]"},{"from":"[1, 2, 2]","to":"[2, 2, 2]"},{"from":"[1, 2, 2]","to":"[1, 3, 2]"},{"from":"[1, 2, 2]","to":"[1, 2, 3]"},{"from":"[1, 1, 3]","to":"[2, 1, 3]"},{"from":"[1, 1, 3]","to":"[1, 2, 3]"},{"from":"[0, 3, 2]","to":"[1, 3, 2]"},{"from":"[0, 3, 2]","to":"[0, 3, 3]"},{"from":"[0, 2, 3]","to":"[1, 2, 3]"},{"from":"[0, 2, 3]","to":"[0, 3, 3]"},{"from":"[3, 3, 0]","to":"[3, 3, 1]"},{"from":"[3, 2, 1]","to":"[3, 3, 1]"},{"from":"[3, 2, 1]","to":"[3, 2, 2]"},{"from":"[3, 1, 2]","to":"[3, 2, 2]"},{"from":"[3, 1, 2]","to":"[3, 1, 3]"},{"from":"[3, 0, 3]","to":"[3, 1, 3]"},{"from":"[2, 3, 1]","to":"[3, 3, 1]"},{"from":"[2, 3, 1]","to":"[2, 3, 2]"},{"from":"[2, 2, 2]","to":"[3, 2, 2]"},{"from":"[2, 2, 2]","to":"[2, 3, 2]"},{"from":"[2, 2, 2]","to":"[2, 2, 3]"},{"from":"[2, 1, 3]","to":"[2, 2, 3]"},{"from":"[2, 1, 3]","to":"[3, 1, 3]"},{"from":"[1, 3, 2]","to":"[2, 3, 2]"},{"from":"[1, 3, 2]","to":"[1, 3, 3]"},{"from":"[1, 2, 3]","to":"[2, 2, 3]"},{"from":"[1, 2, 3]","to":"[1, 3, 3]"},{"from":"[0, 3, 3]","to":"[1, 3, 3]"},{"from":"[3, 3, 1]","to":"[3, 3, 2]"},{"from":"[3, 2, 2]","to":"[3, 3, 2]"},{"from":"[3, 2, 2]","to":"[3, 2, 3]"},{"from":"[3, 1, 3]","to":"[3, 2, 3]"},{"from":"[2, 3, 2]","to":"[3, 3, 2]"},{"from":"[2, 3, 2]","to":"[2, 3, 3]"},{"from":"[2, 2, 3]","to":"[2, 3, 3]"},{"from":"[2, 2, 3]","to":"[3, 2, 3]"},{"from":"[1, 3, 3]","to":"[2, 3, 3]"},{"from":"[3, 3, 2]","to":"[3, 3, 3]"},{"from":"[3, 2, 3]","to":"[3, 3, 3]"},{"from":"[2, 3, 3]","to":"[3, 3, 3]"}];     
            
//            console.log(nodes);
            
            var data = {
                nodes: nodes,
                edges: edges
              };

              var options = {
                layout: {
                    //randomSeed: 966593,
                    hierarchical: {
                        sortMethod: 'directed',
                        //parentCentralization: false,
                        levelSeparation: 150,
                        blockShifting: true,
                        edgeMinimization: true,
                        parentCentralization: true,
                        direction: 'DU',
                        enabled:true
                    },
                  //improvedLayout:true
                },
                /*edges: {
                  arrows: {to : true }
                },*/
                physics: {
                    enabled: true,//auto false, gia na einai pio omorfo
                    barnesHut: {
                      gravitationalConstant: -2000,
                      centralGravity: 0.3,
                      springLength: 95,
                      springConstant: 0.04,
                      damping: 0.09,
                      avoidOverlap: 0
                    },
                    forceAtlas2Based: {
                      gravitationalConstant: -50,
                      centralGravity: 0.01,
                      springConstant: 0.08,
                      springLength: 100,
                      damping: 0.4,
                      avoidOverlap: 0
                    },
                    repulsion: {
                      centralGravity: 0.2,
                      springLength: 200,
                      springConstant: 0.05,
                      nodeDistance: 100,
                      damping: 0.09
                    },
                    hierarchicalRepulsion: {
                      centralGravity: 1,
                      springLength: 100,
                      springConstant: 0.01,
                      nodeDistance: 150,
                      damping: 0.09
                    },
                    maxVelocity: 200,
                    minVelocity: 0.1,
                    solver: 'barnesHut',
                    stabilization: {
                      enabled: true,
                      iterations: 1000,
                      updateInterval: 100,
                      onlyDynamicEdges: false,
                      fit: true
                    },
                    timestep: 0.5,
                    adaptiveTimestep: true
                }
              };
              network = new vis.Network(container, data, options);       
            var doubleClick = false;
            var click = false;
            
            network.on('click', function (properties) {
                setTimeout(function () {
                    if (doubleClick == false) {
                        doOnClick(properties);
                        //click = false;
                    }
                    else {
                        doubleClick = false;
                        //click = false;
                    }
                },400);
            });           
            
            network.on('doubleClick', function (properties) {
                doubleClick = true;
                var node = nodes.get(properties.nodes.toString());
                var res = node.label.replace(/ /g,"");

                res = res.split("");
                var selectedNode;
                
                for ( var i = 1 ; i < res.length-1 ; i = i +2 ){
                    if ( i == 1){
                        selectedNode = res[i];
                    }
                    else{
                        selectedNode = selectedNode + "," + res[i];
                    }
                }

                $.ajax({
                    url: "/action/setselectednode",
                    type: "POST",
                    data : {"selectednode":selectedNode},
                    success : function(result) {
                        window.location.href = "myresults.html";
                    },
                    error : function(xhr, status, error){
                        
                        if(xhr.hasOwnProperty('responseText')){
                            console.log(xhr.responseText);
                            console.log(status.toString());
                            errorHandling(xhr.responseText);
                            alert("Something wrong happened while the anonymized dataset was loading \n"+error.toString());
                            
                        }
                        else{
                            alert("Something wrong happened while the anonymized dataset was loading");
                        }
                    }
                });
            });
            
            function doOnClick(properties) {
                var node = nodes.get(properties.nodes.toString());
                 console.log("res = " + node.label);
                var res = node.label.replaceAll(" ","");
                var selectedNode;
                
                console.log("res = " + res);
                res = res.split("");
                console.log("res length = " + res.length);
                for ( var i = 1 ; i < res.length-1 ; i = i +2 ){
                    if ( i == 1){
                        selectedNode = res[i];
                    }
                    else{
                        selectedNode = selectedNode + "," + res[i];
                    }
                }

                if ( node != null){
                    
                    console.log("selectedNode = " + selectedNode);
                    
                    $.ajax({
                        url: "/action/setselectednode",
                        type: "POST",
                        data : {"selectednode":selectedNode},
                        success : function(result) {
                            $('#popup').modal('show');
                        },
                        error : function(xhr, status, error){
                        
                            if(xhr.hasOwnProperty('responseText')){
                                console.log(xhr.responseText);
                                console.log(status.toString());
                                errorHandling(xhr.responseText);
                                alert("Problem with loading solution \n"+error.toString());
                                
                            }
                            else{
                                alert("Problem with loading solution");
                            }
                        }
                    });              
               }
               else{//den kserw ean kanei kati
                    //click = false;  
               }
            }
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with loading the solution graph \n"+error.toString());
                
            }
            else{
                alert("problem with loading the solution graph");
            }
        }
    }); 
}


//get anonymized dataset
function getAnonDataset(){
    
     $("#spinner").show();
     $.ajax({
        url: "/action/checkdatasetsexistence",
        type: "POST",
        success : function(result) {
            if (result.originalExists == "true"){
                if (result.anonExists !== null){
                    $.ajax({
                        url: "/action/getcolumnnames",
                        type: "POST",
                        success : function(result) {          
                            if ( result != null){
                                var dropzone =  document.getElementById("my-awesome-dropzone");

                                if ( dropzone != null){
                                    dropzone.style.display="none";
                                }

                                var str =  "<thead>" +"<tr>" ;
                                var columnsdef;
                                
                                if(result.columnNames.length > 1){
                                    for (var i = 0 ; i<result.columnNames.length ; i ++ ){
                                        str += "<th style = \"white-space:nowrap;\">" + result.columnNames[i] + "</th>";
                                        if ( i == 0 ){
                                            columnsdef = "[{\"data\":\"" +result.columnNames[i]+"\"},";
                                        }
                                        else if ( i == result.columnNames.length - 1){
                                            columnsdef += "{\"data\":\"" +result.columnNames[i]+"\"}]";
                                        }
                                        else{
                                            columnsdef += "{\"data\":\"" +result.columnNames[i]+"\"},";

                                        }
                                    }
                                }
                                else{
                                    str += "<th>" + result.columnNames[0] + "</th>";
                                    columnsdef = "[{\"data\":\"" +result.columnNames[0]+"\"}]"; 
                                }
                                
                                str += "</<tr>" +"</thead>" ;
                                columnsdef = JSON.parse(columnsdef);
//                                console.log()
                                document.getElementById("anondataset").innerHTML = str;

                                var dataTable = $('#anondataset').DataTable({
                                   "processing": true,
                                   "serverSide": true,
                                   "destroy": true,
                                   "bFilter" : false,
                                   "ordering": false,
                                   

                                   "ajax": {
                                       "dataSrc": "dataAnon",
                                       "url": "/action/getanondataset",
                                       "type": "POST",
                                       "error": function (xhr, status, error) {
                                            if(xhr.hasOwnProperty('responseText')){
                                                console.log(xhr.responseText);
                                                console.log(status.toString());
                                                errorHandling(xhr.responseText);
                                            }
                                            else{
                                                alert("Problem with loading the  anonymized dataset");
                                            }
                                        }
                                    },
                                    
                                    "columns" : columnsdef
                                });
                               
                                $("#anondataset").css("width","100%");
                                $('#popupsampledataset').css("overflow-y","auto");

                                $('#popupsampledataset').on('shown.bs.modal', function() {
                                     var dataTable= $('#anondatasetsol').DataTable();
                                     
                                     dataTable.columns.adjust().responsive.recalc();
                                 });   
                            }
                            $("#spinner").hide();
                        },
                        error : function(xhr, status, error){
                            if(xhr.hasOwnProperty('responseText')){
                                console.log(xhr.responseText);
                                console.log(status.toString());
                                errorHandling(xhr.responseText);
                                alert("Problem with loading the anonymized dataset \n"+error.toString());
                                
                            }
                            else{
                                alert("Problem with loading the  anonymized dataset");
                            }
                            $("#spinner").hide();
                        }
                    });
                }
                else{
                    alert("Choose a solution from the Solution Graph");
                    $("#spinner").hide();
                }
            }
            else if (result.originalExists == "noalgo"){
                alert("You have to execute an algorithm and then choose a solution from the Solution Graph");
                $("#spinner").hide();
            }
        },
        error : function(result){
            alert("Anonymized dataset does not exist");
            $("#spinner").hide();
        }
    }); 
}

//get anonymized and original dataset
function getAnonAndSourceDataset(){
    
    $("#spinner").show();
    $.ajax({
        url: "/action/checkdatasetsexistence",
        type: "POST",
        success : function(result) {
            
            if(result.algorithm === "dp"){
                $("#bothData").hide();
                $("#onlyAnonym").show();
                $("#rulesButton").hide();
                getAnonDataset(); 
                return;
            }
            else{
                $("#onlyAnonym").hide();
                $("#bothData").show();
            }
            
            if(result.algorithm == "clustering" || result.diskData == "true" || result.algorithm === "demographic" || result.algorithm === "dp"){
                $("#rulesButton").hide();
            }
            
            if(result.algorithm == "flash"){
                $("#infoloss").hide();
            }
            if (result.originalExists == "true"){
                if (result.anonExists !== null){
                     $.ajax({
                        url: "/action/getcolumnnames",
                        type: "POST",
                        success : function(result) {
                            if ( result != null){
                                var str =  "<thead>" +"<tr>" ;
                                var columnsdef;

                                for (var i = 0 ; i<result.columnNames.length ; i ++ ){
                                    str += "<th style = \"white-space:nowrap;\">" + result.columnNames[i] + "</th>";
                                    if (result.columnNames.length == 1){
                                        columnsdef = "[{\"data\":\"" +result.columnNames[i]+"\"}]";
                                    }
                                    else if ( i == 0 ){
                                        columnsdef = "[{\"data\":\"" +result.columnNames[i]+"\"},";
                                    }
                                    else if ( i == result.columnNames.length - 1){
                                        columnsdef += "{\"data\":\"" +result.columnNames[i]+"\"}]";
                                    }
                                    else{
                                        columnsdef += "{\"data\":\"" +result.columnNames[i]+"\"},";
                                    }
                                }
                                
                                str += "</<tr>" +"</thead>" ;
                                columnsdef = JSON.parse(columnsdef);

                                document.getElementById("dataset1").innerHTML = str;
                                document.getElementById("anondataset2").innerHTML = str;
                                var original = $('#dataset1').DataTable({
                                   "processing": true,
                                   "serverSide": true,
                                   "ordering": false,
                                   "bFilter" : false,
                                   

                                   "ajax": {
                                       "dataSrc": "data",
                                       "url": "/action/getdataset",
                                       "type": "POST",
                                       "error": function (xhr, status, error) {
                                            if(xhr.hasOwnProperty('responseText')){
                                                console.log(xhr.responseText);
                                                console.log(status.toString());
                                                errorHandling(xhr.responseText);
                                                alert("Problem with loading the anonymized dataset \n"+error.toString());
                                                
                                            }
                                            else{
                                                alert("Problem with loading the  anonymized dataset");
                                            }
                                        }
                                    },
                                    
                                    "columns" : columnsdef
                                });                              
                                
                                var anon = $('#anondataset2').DataTable( {
                                    "processing": true,
                                    "serverSide": true,
                                    "ordering": false,
                                    "bFilter" : false,
                                    "sDom": "lfrti",
                                    "info": false,
                                    "bLengthChange" : false,
                                    

                                    "ajax": {
                                        "dataSrc": "dataAnon",
                                        "url": "/action/getanondataset",
                                        "type": "POST",
                                        "error": function (xhr, status, error) {
                                            if(xhr.hasOwnProperty('responseText')){
                                                console.log(xhr.responseText);
                                                console.log(status.toString());
                                                errorHandling(xhr.responseText);
                                                try{
                                                    var json = JSON.parse(xhr.responseText);
                                                    alert("Problem with loading the anonymized dataset \n"+json.error);
                                                }catch(Exception){
                                                    alert("Problem with loading the anonymized dataset \n"+error.toString());
                                                }
                                                
                                                
                                            }
                                            else{
                                                alert("Problem with loading the  anonymized dataset");
                                            }
                                        }

                                    },
                                    
                                    "columns" : columnsdef,

                                    "rowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ){
                                       
                                        for (var prop in this.fnSettings().jqXHR.responseJSON.toSuppressJson){
                                            attrNames = prop.split(",");
                                            var res = this.fnSettings().jqXHR.responseJSON.toSuppressJson[prop];
                                            var x =[];
                                            
                                            x = res;
                                            for( var i = 0 ; i < x.length ; i ++){
                                                var record =[] 

                                                x[i] =x[i].toString().replace("[","");
                                                x[i] =x[i].replace("]","");
                                                x[i] = x[i].replace(/\s+/, "");
                                                record =  x[i].toString().split(",");
                                                var counter = 0;


                                                
                                                for(var j = 0 ; j < attrNames.length; j ++){
                                                    if (aData[attrNames[j]] == record[j]){
                                                        counter ++;
                                                    }
                                                }
                                               if ( counter == attrNames.length){
                                                    $(nRow).css('color', 'red');
                                                }
                                                
                                            }
                                        }
                                    },
                                });
                                
                                $('#dataset1').on( 'page.dt', function () {
                                    var info = original.page.info();
                                    
                                    var anon = $('#anondataset2').DataTable();
                                    anon.destroy();
                                    anon.clear().draw();
                                    
                                    anon = $('#anondataset2').DataTable({
                                        "processing": true,
                                        "serverSide": true,
                                        "ordering": false,
                                        "bFilter" : false,
                                        "sDom": "lfrti",
                                        "info": false,
                                        "bLengthChange" : false,

                                        "ajax": {
                                            "dataSrc": "dataAnon",
                                            "url": "/action/getanondataset",
                                            "type": "POST",
                                            "data": {start:info.page*info.length,length:info.length},
                                            "error": function (xhr, status, error) {
                                                if(xhr.hasOwnProperty('responseText')){
                                                    console.log(xhr.responseText);
                                                    console.log(status.toString());
                                                    errorHandling(xhr.responseText);
                                                    try{
                                                        var json = JSON.parse(xhr.responseText);
                                                        alert("Problem with loading the anonymized dataset \n"+json.error);
                                                    }catch(Exception){
                                                        alert("Problem with loading the anonymized dataset \n"+error.toString());
                                                    }
                                                    
                                                }
                                                else{
                                                    alert("Problem with loading the  anonymized dataset");
                                                }
                                            }
                                         },

                                         "columns" : columnsdef,
                                         "rowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ){
                                       
                                            for (var prop in this.fnSettings().jqXHR.responseJSON.toSuppressJson){
                                                attrNames = prop.split(",");
                                                var res = this.fnSettings().jqXHR.responseJSON.toSuppressJson[prop];
                                                var x =[];

                                                x = res;
                                                for( var i = 0 ; i < x.length ; i ++){
                                                    var record =[] 

                                                    x[i] =x[i].toString().replace("[","");
                                                    x[i] =x[i].replace("]","");
                                                    x[i] = x[i].replace(/\s+/, "");
                                                    record =  x[i].toString().split(",");
                                                    var counter = 0;

                                                    for(var j = 0 ; j < attrNames.length; j ++){
                                                        if (aData[attrNames[j]] == record[j]){
                                                            counter ++;
                                                        }
                                                    }

                                                    if ( counter == attrNames.length){
                                                        $(nRow).css('color', 'red');
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    
                                    
                                    console.log("change pageeeeeeeee = " + info.length );
                                } );
                                
                                $('#dataset1').on( 'length.dt', function ( e, settings, len ) {
                                    var info = original.page.info();
                                    console.log( 'New page length: '+len +"\tpage = " + info.page );
                                    var anon = $('#anondataset').DataTable();
                                    anon.destroy();
                                    
                                    anon = $('#anondataset').DataTable({
                                        "processing": true,
                                        "serverSide": true,
                                        "ordering": false,
                                        "bFilter" : false,
                                        "sDom": "lfrti",
                                        "info": false,
                                        "bLengthChange" : false,

                                        "ajax": {
                                            "dataSrc": "dataAnon",
                                            "url": "/action/getanondataset",
                                            "type": "POST",
                                            "data": {start:info.page*len,length:len},
                                            "error": function (xhr, status, error) {
                                                if(xhr.hasOwnProperty('responseText')){
                                                    console.log(xhr.responseText);
                                                    console.log(status.toString());
                                                    errorHandling(xhr.responseText);
                                                    try{
                                                        var json = JSON.parse(xhr.responseText);
                                                        alert("Problem with loading the anonymized dataset \n"+json.error);
                                                    }catch(Exception){
                                                        alert("Problem with loading the anonymized dataset \n"+error.toString());
                                                    }
                                                    
                                                }
                                                else{
                                                    alert("Problem with loading the  anonymized dataset");
                                                }
                                            }
                                         },

                                         "columns" : columnsdef,
                                         "rowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ){
                                       
                                            for (var prop in this.fnSettings().jqXHR.responseJSON.toSuppressJson){
                                                attrNames = prop.split(",");
                                                var res = this.fnSettings().jqXHR.responseJSON.toSuppressJson[prop];
                                                var x =[];

                                                x = res;
                                                for( var i = 0 ; i < x.length ; i ++){
                                                    var record =[] 

                                                    x[i] =x[i].toString().replace("[","");
                                                    x[i] =x[i].replace("]","");
                                                    x[i] = x[i].replace(/\s+/, "");
                                                    record =  x[i].toString().split(",");
                                                    var counter = 0;

                                                    for(var j = 0 ; j < attrNames.length; j ++){
                                                        if (aData[attrNames[j]] == record[j]){
                                                            counter ++;
                                                        }
                                                    }

                                                    if ( counter == attrNames.length){
                                                        $(nRow).css('color', 'red');
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    
                                } );

                            }
                            $("#spinner").hide();
                        },
                        error : function(xhr, status, error){
                            if(xhr.hasOwnProperty('responseText')){
                                console.log(xhr.responseText);
                                console.log(status.toString());
                                errorHandling(xhr.responseText);
                                alert("Unable loading columns' names \n"+error.toString());
                                
                            }
                            else{
                                alert("Unable loading columns' names");
                            }
                            $("#spinner").hide();
                        }
                    });
                }
                else{
                  
                    getOriginalDataForResultsPage();
//                    alert("Choose a solution from the Solution Graph");
                    $("#spinner").hide();
                }
            }
            else if (result.originalExists == "noalgo"){
             
                getOriginalDataForResultsPage();
//                alert("You have to execute an algorithm and then choose a solution from the Solution Graph");
                $("#spinner").hide();
            }
            else{
                alert("You must upload dataset")
            }
            $("#spinner").hide();
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Original dataset does not exist\n"+error.toString());
                
            }
            else{
                alert("Original dataset does not exist");
            }
            $("#spinner").hide();
        }
       
    });   
}


//get only original dataset for the result page
function getOriginalDataForResultsPage(){
    
    $.ajax({
        url: "/action/getcolumnnames",
        type: "POST",
        success : function(result) {
            if ( result != null){
                var str =  "<thead>" +"<tr>" ;
                var columnsdef;

                for (var i = 0 ; i<result.columnNames.length ; i ++ ){
                    str += "<th style = \"white-space:nowrap;\">" + result.columnNames[i] + "</th>";
                    if ( i == 0 ){
                        columnsdef = "[{\"data\":\"" +result.columnNames[i]+"\"}";
                    }
                    else if ( i == result.columnNames.length - 1){
                        columnsdef += ",{\"data\":\"" +result.columnNames[i]+"\"}";
                    }
                    else{
                        columnsdef += ",{\"data\":\"" +result.columnNames[i]+"\"}";
                    }
                }
                
                columnsdef = columnsdef +"]";
                str += "</<tr>" +"</thead>" ;
                columnsdef = JSON.parse(columnsdef);

                document.getElementById("dataset1").innerHTML = str;
                $('#dataset1').DataTable( {
                   "processing": true,
                   "serverSide": true,
                   "ordering": false,
                   "bFilter" : false,

                   "ajax": {
                       "dataSrc": "data",
                       "url": "/action/getdataset",
                       "type": "POST",
                       "error": function (xhr, status, error) {
                            if(xhr.hasOwnProperty('responseText')){
                                console.log(xhr.responseText);
                                console.log(status.toString());
                                errorHandling(xhr.responseText);
                                alert("Problem with loading the original dataset \n"+error.toString());
                            }
                            else{
                                alert("Problem with loading the  original dataset");
                            }
                        }

                   },
                   
                   "columns" : columnsdef
               } );
            }
            else{
                alert("Original dataset does not exist");
            }
        },
        error : function(result){
            alert("Original dataset does not exist");
        }
    });
}


//save dataset 
function saveDataset(){

    $.fileDownload('/action/savedataset')
    .done(function () { alert('File download a success!'); })
    .fail(function () { alert('File download failed!'); });
}


//save hierarchy
function saveHierarchy(){
    
    $.fileDownload('/action/savehierarchy')
    .done(function () { alert('File download a success!'); })
    .fail(function () { alert('File download failed!'); }); 
}


//save anonimized dataset
function saveAnonymizeDataset(){
    //console.log("edwwwwwwwwwwwwwwwwwwwwwww");
    $("#spinner").show();
    console.log("window.onbeforeunload = " + $(window.onbeforeunload));
    
    /*window.onbeforeunload = function() {
        cleanUserData();
    };*/
    
      /*$('a[rel!=ext]').click(function() { window.onbeforeunload = null; });
            $('button[rel!=ext]').click(function() { window.onbeforeunload = null; });
             $('div[rel!=ext]').click(function() { window.onbeforeunload = null; });*/
    
    $.fileDownload('/action/saveanonymizedataset')
    .done(function () { alert('File download a success!');  $("#spinner").hide();})
    .fail(function () { alert('File download failed!');  $("#spinner").hide();});
    
    $("#spinner").hide();
    
    
}

//global Variable
var zenTable = null;


//get similar files from zenodo
function getSimilarZenodoFiles(usertoken,filename,title,keywords){

    $( "#spinner" ).show();
    if ( zenTable !== null){
        zenTable.destroy();
    }
    
    zenTable = $('#zenodosimilarfiles').DataTable( {
        "processing": true,
        "serverSide": false,
        "paging":   false,
        "info":     false,
        "order": [[ 6, "desc" ]],
        "bFilter": false,
        "searching": false,

        "ajax": {
           "url": "/action/getsimilarzenodofiles",
           "type": "POST",
           "data":{
                "usertoken":usertoken,"filename":filename,"title":title,"keywords":keywords
            },

            "error": function(result){
                alert("getSimilarZenodoFiles: Wrong Zenodo token");
                $( "#spinner" ).hide();
                window.location ="myzenodosavewizard.html";
            }
        },
        
        "columns": [
            { "data": "fileName" },
            { "data": "title" },
            { "data": "keywords" },
            { "data": "created" },
            { "data": "modified" },
            { "data": "filesize" },
            { "data": "percentage" }
        ],
        "initComplete": function(settings, json) {
             $( "#spinner" ).hide();
         }

    });
}


//save files to zenodo
function saveToZenodo(url){
    
    $.ajax({
        url: "/action/saveurltoreturn",
        type: "POST",
        data: {url:url},
        success : function(result) {
            window.location = "myzenodosavewizard.html";
        },
        error : function(result){
            alert("saveToZenodo: Server problem");
        }
    });
}

function saveToDataverse(url){
    
    $.ajax({
        url: "/action/saveurltoreturn",
        type: "POST",
        data: {url:url},
        success : function(result) {
            window.location = "mydataversesavewizard.html";
        },
        error : function(result){
            alert("saveToDataverse: Server problem");
        }
    });
}


//get all zenodo files
function getDataversefiles(usertoken, server_url, dataset_id){
    $("#spinner").show();
    
    var table = $('#dataversefiles').DataTable({
        "processing": true,
        "serverSide": true,
        "paging":   false,
        "info":     false,
        "searching": false,
        "ordering": false,
        
        "ajax": {
           "url": "/action/getdataversefiles",
           "type": "POST",
           "data":{
                "usertoken":usertoken,
                "server_url":server_url,
                "dataset_id":dataset_id
            },

            "error": function(result){
                alert("Something wrong happened, please try again!");
                $( "#spinner" ).hide();
                window.location = "index.html" ;
            }
        },
        "columns": [
            { "data": "fileName" },
            { "data": "description" },
            { "data": "categories"},
            { "data": "type" },
            { "data": "created" },
            { "data": "filesize" }
        ],
        "initComplete": function(settings, json) {
             $( "#spinner" ).hide();
         }
        
    });
    
    table.destroy();
    
    $('#dataversefiles tbody').on('click', 'tr', function () {
        var data = table.row(this).data();

        $.ajax({
            url: "/action/loaddataversefile",
            type: "POST",
            data: {filename:data.fileName,type:data.type,size:data.filesize,usertoken:usertoken,server_url:server_url},
            success : function(result) {
                if(result == "error"){
                    alert("Something wrong happened, please try again!");
                }
                else{
                    window.location = "mywizard.html";
                }
            },
            error : function(result){
                alert("Something wrong happened, please try again!");
            }
        });
    }); 
}
function getZenodoFiles(usertoken){
    
   $("#spinner").show();
    
    var table = $('#zenodofiles').DataTable({
        "processing": true,
        "serverSide": true,
        "paging":   false,
        "info":     false,
        "searching": false,

        "ajax": {
           "url": "/action/getzenodofiles",
           "type": "POST",
           "data":{
                "usertoken":usertoken
            },

            "error": function(result){
                alert("getZenodoFiles: Wrong Zenodo token");
                $( "#spinner" ).hide();
                window.location = "index.html" ;
            }
        },
        "columns": [
            { "data": "fileName" },
            { "data": "title" },
            { "data": "keywords" },
            { "data": "created" },
            { "data": "modified" },
            { "data": "filesize" }
        ],
        "initComplete": function(settings, json) {
             $( "#spinner" ).hide();
         }
    });
    
    table.destroy();
    

    $('#zenodofiles tbody').on('click', 'tr', function () {
        var data = table.row(this).data();

        $.ajax({
            url: "/action/loadzenodofile",
            type: "POST",
            data: {filename:data.fileName,title:data.title,usertoken:usertoken},
            success : function(result) {
                window.location = "mywizard.html";
            },
            error : function(result){
                alert("getZenodoFiles: Server problem");
                
            }
        });
    }); 
    
}


//save file to zenodo
function saveFileToZenodo(usertoken, author, affiliation, filename , title, description, contributors, keywords){

    $.ajax({
        url: "/action/savefiletozenodo",
        type: "POST",
        data: {usertoken:usertoken,author:author, affiliation:affiliation,filename:filename,title:title,description:description, contributors:contributors, keywords:keywords},
        success : function(result) {
            console.log("result = " +result);
            alert("The file was saved successfully in Zenodo!");
            window.location = result;/////mipws edw thelei amnesia kai to exw ksexasei??
        },
        error : function(result){
            alert("saveFileToZenodo: Server problem");
             //window.location ="myzenodosavewizard.html";
        }
    });
}

function saveFileToDataverse(usertoken, descr, server_url, dataset_id){

    $.ajax({
        url: "/action/savefiletodataverse",
        type: "POST",
        data: {usertoken:usertoken, descr:descr, server_url:server_url,dataset_id:dataset_id},
        success : function(result) {
            console.log("result = " +result);
            alert("The file was saved successfully in Dataverse!");
            window.location = result;/////mipws edw thelei amnesia kai to exw ksexasei??
        },
        error : function(result){
            alert("saveFileToDataverse: Server problem");
             //window.location ="myzenodosavewizard.html";
        }
    });
}


//get types and names of all quasi-identifiers
function getColumnNamesAndTypes(){
    
    $.ajax({
        url: "/action/getattrtypes",
        type: "POST",
        success : function(result) {
            var str;
            
            for (  var i = 0 ; i < result.columnNames.length ; i ++){
                if ( i === 0){
                    str =  "<option>" + result.columnNames[i] + "</option>";
                }
                else{
                    str = str + "<option>" + result.columnNames[i] + "</option>";
                }
            }

            document.getElementById("onattribute").innerHTML = str;
            
            return result;
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Unable loading attributes columns' names \n"+error.toString());
                
            }
            else{
                alert("Unable loading attributes columns' names");
            }
        }
    });
}


//auto generatte hierarchy
function autogenerate(typehier,vartype,onattribute,step,sorting,hiername, fanout, limits, months, days, years,length){
    console.log("autogenerate js "+step+" "+typehier+" "+vartype+" "+onattribute+" "+sorting+" "+hiername+" "+fanout+" "+limits," "+length);
    $.ajax({
        url: "/action/autogeneratehierarchy",
        type: "POST",
        data :{typehier : typehier,vartype:vartype,onattribute:onattribute,step:step,sorting:sorting,hiername:hiername, fanout:fanout, limits:limits, months:months, days:days, years:years, length:length},
        success : function(result) {
            console.log("result = "+result );
            window.location.href ="myhier.html"; 
        },
        error : function(xhr, status, error){
//            console.log("result = "+result );
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                try{
                    var json = JSON.parse(xhr.responseText);
                    alert("Something wrong happened while hierarchy was autogenerated \n"+json.error);
                    window.location = "index.html";
                }catch(Exception){
                    alert("Something wrong happened while hierarchy was autogenerated \n"+error.toString());
                }
                
            }
            else{
                alert("Something wrong happened while hierarchy was autogenerated");
            }
        }
    });
}

//get statistics for a specific solution
function getStatistics(){
    
    $("#spinner").show();
    $("#solutionstatistics").hide();
    $.ajax({
        url: "/action/findsolutionstatistics",
        type: "POST",
        success : function(result) {
            var attr = result.toString().split(',');
            
            str = "<div class=\"form-group\"><label class=\"col-lg-2 control-label\"> Select attributes :</label><div class=\"col-lg-10\">";                           
            str += "<select class=\"attrstatistics form-control\">"; 
            
            for( var i = 0 ; i < attr.length ; i ++){
                str += "<option value=\"" + attr[i] +"\">" + attr[i] + "</option>";
            }
            str += "</select> </div> </div>";
   
            document.getElementById("attributestatistics").innerHTML = str;
            
            $.ajax({
                url: "/action/getsolutionstatistics",
                type: "GET",
                cache: false,
                async: false,
                data :{selectedattributenames:attr[0]},
                success : function(result2) {
                    if (result2.endPage == 1 ){
                        console.log("result2.endPage1111 = " + result2.endPage);
                    }
                    else{
                        console.log("result2.endPage2222 = " + result2.endPage);
                    }
                    
                    $('#solutionstatistics').css({'width':'90%' , 'min-height':'150px'});
                    var str = "Percentage of displayed dataset is : " + result2.pagePercentage + "%";
                    document.getElementById("percentagesolution").innerHTML = str;
                    
                   // console.log("result2.percentangeSuppress = " + result2.percentangeSuppress);
                    
                    if (result2.percentageSuppress != 0){
                        str = "To produce a k = " + result2.k + " suppress " + result2.percentageSuppress + "%";
                        document.getElementById("percentangeSuppress").innerHTML = str;
                        document.getElementById('percentangeSuppress').style.visibility = 'visible';
                    }                   
                    else{
                       document.getElementById('percentangeSuppress').style.visibility = 'hidden';

                    }
                    

                    if (result2.suppress == false){
                        document.getElementById("suppressbutton").disabled = true;
                    }
                    else{
                        document.getElementById("suppressbutton").disabled = false;
                    }
                    
                    document.getElementById("applysource").disabled = true;
                    var plotObj = $.plot($("#solutionstatistics"), result2.solutions, {
                        series: {
                            pie: {
                                show: true,
                                label:{
                                    show:false
                                }
                            }
                        },
                        grid: {
                            hoverable: true
                        },
                        tooltip: true,
                        tooltipOpts: {
                            valueDecimals: 2,
                            content: "%p.1%, %s", // show percentages, rounding to 2 decimal places
                            shifts: {
                                x: 80,
                                y: 0
                            },
                            
                            defaultTheme: true
                        },
                        legend: {
                            show: false
                        }
                    });
                    $("#spinner").hide();
                    $("#solutionstatistics").show();
                },
                error : function(xhr, status, error){
                    if(xhr.hasOwnProperty('responseText')){
                        console.log(xhr.responseText);
                        console.log(status.toString());
                        errorHandling(xhr.responseText);
                        alert("Unable to get statistics\n"+error.toString());
                        
                    }
                    else{
                        alert("Unable to get statistics");
                    }
                    $("#spinner").hide();
                }
            });
            
            attributestatistics.addEventListener("click", function() {
                console.log("check listtttttttt");
                $("#spinner").show();
                $("#solutionstatistics").hide();
                $.ajax({
                    url: "/action/getsolutionstatistics",
                    type: "GET",
                    cache: false,
                    async: false,
                    data :{selectedattributenames:$(".attrstatistics").val()},
                    success : function(result2) {
                        
                        if (result2.suppress == false){
                            document.getElementById("suppressbutton").disabled = true;
                        }
                        else{
                            document.getElementById("suppressbutton").disabled = false;
                        }

                        var str = "Percentage of displayed dataset is : " + result2.pagePercentage + "%";
                        document.getElementById("percentagesolution").innerHTML = str;
                        
                        //console.log("result2.percentangeSuppress = " + result2.percentangeSuppress);
                        if (result2.percentageSuppress != 0){
                            str = "To produce a k = " + result2.k + " suppress "  + result2.percentageSuppress + "%";
                            document.getElementById("percentangeSuppress").innerHTML = str;
                            document.getElementById('percentangeSuppress').style.visibility = 'visible';
                        }
                        else{
                           document.getElementById('percentangeSuppress').style.visibility = 'hidden';
                            
                        }
                        
                        var plotObj = $.plot($("#solutionstatistics"), result2.solutions, {
                            series: {
                                pie: {
                                    show: true,
                                    label:{
                                        show:false
                                    }
                                }
                            },
                            grid: {
                                hoverable: true
                            },
                            tooltip: true,
                            tooltipOpts: {
                                valueDecimals: 2,
                                content: "%p.0%, %s", // show percentages, rounding to 2 decimal places
                                shifts: {
                                    x: 20,
                                    y: 0
                                },
                                defaultTheme: true
                            },
                            legend: {
                                show: false
                            }
                        });
                        $("#spinner").hide();
                        $("#solutionstatistics").show();
                    },
                    error : function(xhr, status, error){
                        if(xhr.hasOwnProperty('responseText')){
                            console.log(xhr.responseText);
                            console.log(status.toString());
                            errorHandling(xhr.responseText);
                            alert("Unable to get statistics\n"+error.toString());
                            
                        }
                        else{
                            alert("Unable to get statistics");
                        }
                        $("#spinner").hide();
                    }
                });
            });
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Unable to get solution statistics\n"+error.toString());
                
            }
            else{
                alert("Unable to get solution statistics");
            }
            $("#spinner").hide();
        }
    });
}


//get names of columnes for modal
function getColumnNamesModal(){
    
    $("#spinner").show();
    $.ajax({
        url: "/action/getcolumnnames",
        type: "POST",
        data :{},
        success : function(result) {
            var str ;
            
            for ( var i = 0 ; i < result.columnNames.length ; i++){
                if ( i == 0){
                    str = "<div class=\"i-checks\"><label> <input type=\"checkbox\" value=\"" + result.columnNames[i] +"\" id=\"checkattr" + i + "\" > <i></i> " + result.columnNames[i] + "</label></div>";
                }
                else{
                    str = str + "<div class=\"i-checks\"><label> <input type=\"checkbox\" value=\"" + result.columnNames[i] +"\" id=\"checkattr" + i + "\" > <i></i> " + result.columnNames[i] + "</label></div>";
                }
            }
            console.log("hereeeeeeeeeeeeeeeeeeeeeee");
            $("#spinner").hide();
            document.getElementById("checkattributes").innerHTML = str;
        },
        error : function(xhr, status, error){
            $("#spinner").hide();
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with columns' names\n"+error.toString());
                
            }
            else{
                alert("Problem with columns' names");
            }
        }
    });
}


//get attributes from modal
function getAttributesFromModal(){
        
    document.getElementById("applysource").disabled = true; 
    $("#spinner").show();
    $.ajax({
        url: "/action/getcolumnnames",
        type: "POST",
        data :{},
        success : function(result) {
            var str = null;
            var FLAG = false;
            var attrName ;
            
            for ( var i = 0 ; i < result.columnNames.length ; i++){
                var element = document.getElementById("checkattr" + i);
                if($(element).is(':checked')){ 
                    if ( FLAG == false){
                        str = i;
                        FLAG = true;
                        attrName = $("#checkattr" + i).val();
                    }
                    else{
                        str = str +"," + i;
                        attrName = attrName + "," + $("#checkattr" + i).val(); 
                    }
                }   
            }
            
            var selectedAttr = str ;
            var kcheck = document.getElementById('Kcheck').value;

            $.ajax({
                url: "/action/checkdataset",
                type: "POST",
                data:{attributes:selectedAttr},
                success : function(k) {
                    $.ajax({
                        url: "/action/getsourcestatistics",
                        type: "GET",
                        data :{kcheck:kcheck,selectedattribute:selectedAttr,selectedattributenames:attrName},
                        success : function(result2) {
                            $('#popupcheck').modal('show');
                            $('#solutionsourcestatistics').css({'width':'100%' , 'min-height':'150px'});
                            var str = " \"" + attrName +"\" is anonymous for k : " + k +"."; 
                            document.getElementById("ksourcedata").innerHTML = str;
                            var str = "Percentage of displayed dataset is : " + result2.pagePercentage + "%";
                            document.getElementById("percentagesource").innerHTML = str;

                            console.log("edwwww = " + result2.percentangeSuppress);

                            if (result2.percentangeSuppress != 0){
                                str = "To produce a k = " + kcheck + " suppress " + result2.percentageSuppress + "%";
                                document.getElementById("percentangeSuppress").innerHTML = str;
                                document.getElementById('percentangeSuppress').style.visibility = 'visible';
                            }                   
                            else{
                               document.getElementById('percentangeSuppress').style.visibility = 'hidden';

                            }

                            var plotObj = $.plot($("#solutionsourcestatistics"), result2.solutions, {
                                series: {
                                    pie: {
                                        show: true,
                                        label:{
                                            show:false
                                        }
                                    }
                                },
                                grid: {
                                    hoverable: true
                                },
                                tooltip: true,
                                tooltipOpts: {
                                    content: "%p.0%, %s", // show percentages, rounding to 2 decimal places "%p.0%, %s"
                                    shifts: {
                                        x: 20,
                                        y: 0
                                    },
                                    defaultTheme: true
                                },
                                legend: {
                                    show: false
                                }
                            });

                            if (result2.suppress == false){
                                document.getElementById("suppressbutton").disabled = true;
                            }
                            else{
                                document.getElementById("suppressbutton").disabled = false;
                            }
                            document.getElementById("applysource").disabled = true;
                            $("#spinner").hide();
                        },
                        error : function(xhr, status, error){
                            if(xhr.hasOwnProperty('responseText')){
                                console.log(xhr.responseText);
                                console.log(status.toString());
                                errorHandling(xhr.responseText);
                                alert("You must choose k and attributes\n"+error.toString());
                                
                            }
                            else{
                                alert("You must choose k and attributes");
                            }
                            $("#spinner").hide();
                        }
                    });
                },
                error : function(xhr, status, error){
                    if(xhr.hasOwnProperty('responseText')){
                        console.log(xhr.responseText);
                        console.log(status.toString());
                        errorHandling(xhr.responseText);
                        alert("Dataset does not exist\n"+error.toString());
                        
                    }
                    else{
                        alert("Dataset does not exist");
                    }
                    $("#spinner").hide();
                }
            });
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Unable loading columns' names\n"+error.toString());
                
            }
            else{
                alert("Unable loading columns' names");
            }
            $("#spinner").hide();
        }
    });
}
  

//restart
function restart(){
    
    $.ajax({
        url: "/action/restart",
        type: "POST",
        data :{},
        success : function(result) {
            window.location = "index.html";
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Server is down\n"+error.toString());
               
            }
            else{
                alert("Server is down");
            }
        }
    });
}

function getInfoLoss(){
    $("#spinner").show();
    $.ajax({
        url: "/action/dataquality",
        type: "POST",
        data :{},
        success : function(result) {
            console.log(result)
            var ncp = ((result.NCP*100).toFixed(2));
            var total = ((result.Total*100).toFixed(2)); 
//            $("#ncpVal").empty().append(ncp);
            var ctx = document.getElementById('circularLoader').getContext('2d');
            var ctx2 = document.getElementById('circularLoaderTotal').getContext('2d');
            var al = 0;
            var start = 4.72;
            var cw = ctx.canvas.width;
            var ch = ctx.canvas.height; 
            var diff;
            function progressSim(proportion,s,canv){
                    diff = ((al / 100) * Math.PI*2*10).toFixed(2);
                    canv.clearRect(0, 0, cw, ch);
                    canv.lineWidth = 17;
                    canv.fillStyle = '#C8C8C8';
                    canv.strokeStyle = "#C8C8C8";
                    canv.beginPath();
                    canv.arc(100, 100, 75, 0, 2 * Math.PI);
                    canv.stroke();


                    canv.lineWidth = 17;
                    canv.fillStyle = '#19aa8d';
                    canv.strokeStyle = "#19aa8d";
                    canv.textAlign = "center";
                    canv.font="18px sans-serif";
                    canv.fillText(proportion+'%', cw*.52, ch*.5+5, cw+12);
                    canv.beginPath();
                    canv.arc(100, 100, 75, start, diff/10+start, false);
                    canv.stroke();
                    if(al >= proportion){
                            if( s === "NCP"){
                                clearTimeout(sim);
                            }
                            else if(s === "Total"){
                                clearTimeout(sim2);
                            }
                        // Add scripting here that will run when progress completes
                    }
                    al++;
            }
            var sim,sim2;
            sim = setInterval(progressSim.bind(null,ncp,"NCP",ctx), 50);
            sim2 = setInterval(progressSim.bind(null,total,"Total",ctx2), 50)
    //            $("#ncp").append("<div class=\"myprogress-bar\" data-percent=\""+ncp+"\" data-duration=\"5000\" ></div>")
            $("#spinner").hide();
            $("#infoMetrics").modal('show');
//            $(".myprogress-bar").loading();
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Something wrong happened\n"+error.toString());
                
            }
            else{
                alert("Something wrong happened, please try again!");
            }
        }
    }); 
    
}

function checkSuppression(){
    $.ajax({
        url: "/action/checksuppress",
        type: "POST",
        data :{},
        success : function(result) {
            console.log(result)
            if(result){
                $("#infoloss").removeClass("hidden");
            }
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Something wrong happened\n"+error.toString());
                
            }
            else{
                alert("Something wrong happened, please try again!");
            }
        }
    }); 
}

//delete suppress
function deleteSuppress(){

    $.ajax({
        url: "/action/deletesuppress",
        type: "POST",
        data :{},
        success : function(result) {
            
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with deleting\n"+error.toString());
                
            }
            else{
                alert("Problem with deleting");
            }
        }
    }); 
}


//suppress values
function suppressValues(source){
    
    $.ajax({
        url: "/action/suppress",
        type: "POST",
        data :{},
        success : function(result) {
            if ( source == 'true'){
                
                $('#solutionsourcestatistics').css({'width':'100%' , 'min-height':'150px'});
                if ( result.solutions == ""){                  
                    document.getElementById('ksourcedata').style.visibility = 'hidden';
                    var str = " All the dataset deleted";
                    document.getElementById("percentagesource").innerHTML = str;
                    var plotObj = $.plot($("#solutionsourcestatistics"), result.solutions, {
                        series: {
                            pie: {
                                show: true,
                                label:{
                                    show:false
                                }
                            }
                        },
                        grid: {
                            hoverable: true
                        },
                        tooltip: true,
                        tooltipOpts: {
                            content: "%p.0%, %s", // show percentages, rounding to 2 decimal places
                            shifts: {
                                x: 20,
                                y: 0
                            },
                            defaultTheme: true
                        },
                        legend: {
                            show: false
                        }
                    });
                    document.getElementById("applysource").disabled = true; 
                }
                else{
                    var str = " Percentage of showing dataset is : " + result.pagePercentage + "%";
                    document.getElementById("percentagesource").innerHTML = str;
                    
                    /*if (result.endPage == 1){
                        document.getElementById('showmoresourcesolutions').style.visibility = 'hidden';
                    }
                    else{
                        document.getElementById('showmoresourcesolutions').style.visibility = 'visible';
                    }*/
                    

                    var plotObj = $.plot($("#solutionsourcestatistics"), result.solutions, {
                        series: {
                            pie: {
                                show: true,
                                label:{
                                    show:false
                                }
                            }
                        },
                        grid: {
                            hoverable: true
                        },
                        tooltip: true,
                        tooltipOpts: {
                            content: "%p.0%, %s", // show percentages, rounding to 2 decimal places
                            shifts: {
                                x: 20,
                                y: 0
                            },
                            defaultTheme: true
                        },
                        legend: {
                            show: false
                        }
                    });
                    document.getElementById("applysource").disabled = false; 
                }
                document.getElementById("suppressbutton").disabled = true;
                document.getElementById('percentangeSuppress').style.visibility = 'hidden';
            }
            else{
                document.getElementById('percentangeSuppress').style.visibility = 'hidden';
                if ( result.solutions == ""){
                    $('#solutionstatistics').css({'width':'90%' , 'min-height':'150px'});
                    var str = "All the dataset deleted";
                    document.getElementById("percentagesolution").innerHTML = str;

                    var plotObj = $.plot($("#solutionstatistics"), result.solutions, {
                        series: {
                            pie: {
                                show: true,
                                label:{
                                    show:false
                                }
                            }
                        },
                        grid: {
                            hoverable: true
                        },
                        tooltip: true,
                        tooltipOpts: {
                            content: "%p.0%, %s", // show percentages, rounding to 2 decimal places
                            shifts: {
                                x: 20,
                                y: 0
                            },

                            defaultTheme: true
                        },
                        legend: {
                            show: false
                        }
                    });
                    document.getElementById("applysource").disabled = false;
                    document.getElementById("suppressbutton").disabled = true;
                } 
                else{
                    document.getElementById("applysource").disabled = false;
                    document.getElementById("suppressbutton").disabled = true;

                    $('#solutionstatistics').css({'width':'90%' , 'min-height':'150px'});
                    var str = "Percentage of showing dataset is : " + result.pagePercentage + "%";
                    document.getElementById("percentagesolution").innerHTML = str;

                    var plotObj = $.plot($("#solutionstatistics"), result.solutions, {
                        series: {
                            pie: {
                                show: true,
                                label:{
                                    show:false
                                }
                            }
                        },
                        grid: {
                            hoverable: true
                        },
                        tooltip: true,
                        tooltipOpts: {
                            content: "%p.0%, %s", // show percentages, rounding to 2 decimal places
                            shifts: {
                                x: 20,
                                y: 0
                            },

                            defaultTheme: true
                        },
                        legend: {
                            show: false
                        }
                    });
                }
            }
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with suppressing\n"+error.toString());
                
            }
            else{
                alert("Problem with suppressing");
            }
        }
    });
}


//queries statistics for the anonymized dataset
function queriesStatistics(){
    
    $.ajax({
        url: "/action/getcolumnnamesandtypes",
        type: "POST",
        success : function(result) {
            var str = "<ul class=\"todo-list m-t\">";
            
            for ( var i = 0 ; i < result.length ; i ++ ){
                str = str +"<li>"
                str = str + "<input id= \"" + result[i].columnName + "\" onchange= \"statisticsQueryListener('" + result[i].columnName + "');\" type=\"checkbox\" value=\"\" name=\"\" class=\"i-checks\"/><span class=\"m-l-xs\"> " + result[i].columnName + "</span> &nbsp;<div class=\"nav navbar-top-links navbar-right\">"
                if ( result[i].type == "distinct"){
                    str = str + "<input id=\"" +result[i].columnName + "value\" type=\"hidden\" title=\"Set the desired value\" placeholder=\"value\" id=\"exampleInputEmail2\">";   
                }
                else{
                    str = str + "<input id=\"" +result[i].columnName + "min\" type=\"hidden\" title=\"Set the minimum value\" placeholder=\"min\" id=\"exampleInputEmail2\">";
                    str = str + "<input id=\"" +result[i].columnName + "max\" type=\"hidden\" title=\"Set the maximum value\"placeholder=\"max\" id=\"exampleInputEmail2\">";
                }
                str = str +"</div></li>"
            }

            str = str + "</ul>";
            document.getElementById("queriesinput").innerHTML = str;
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Problem with loading columns' info\n"+error.toString());
                
            }
            else{
                alert("Problem with loading columns' info");
            }
        }
    }); 
}


//show identifiers for the queries
function statisticsQueryListener(columnName){
    var element = document.getElementById(columnName);
    if($(element).is(':checked')){ 
        if ( document.getElementById(columnName+"value") != null){
            document.getElementById(columnName+"value").type = 'visible';
        }
        else{
            document.getElementById(columnName+"min").type = 'visible';
            document.getElementById(columnName+"max").type = 'visible';
        }
    }
    else{
        if ( document.getElementById(columnName+"value") != null){
            document.getElementById(columnName+"value").type = 'hidden';
        }
        else{
            document.getElementById(columnName+"min").type = 'hidden';
            document.getElementById(columnName+"max").type = 'hidden';
        }
    }
}


//get proper algorithm based on type of dataset
function getProperAlgorithm(){
    
    $.ajax({
        url: "/action/getproperalgorithm",
        type: "POST",
        success : function(result) {
            var str, strTitle;
            $("#algoselection").hide();
            $("#algorithm_title").show();
            if ( result == "set"){
                strTitle = "k<sup>m</sup>-Anonymization" //"Apriori";
                str = "<option value=\"apriori\">Apriori</option>";
                document.getElementById("mlabel").style.visibility = 'visible';
                $("#mlabel").show();
            }
            else if(result == "relset"){
                strTitle = "k<sup>m</sup>-Anonymization" //"Apriori for Object Relational Data"
                str = "<option value=\"mixedapriori\">Mixed Apriori</option>";
                document.getElementById("mlabel").style.visibility = 'visible';
                $("#mlabel").show();
            }
            else if(result == "disk"){
                strTitle = "Disk based Clustering"
                document.getElementById("mlabel").style.visibility = 'hidden';
                $("#mlabel").hide();
                str = "<option value=\"clustering\">Disk based Clustering</option>";
//                str += "<option value=\"dp\">Differential Privacy</option>";
                $("#algorithm_title").hide();
                $("#algoselection").show();
            }
            else if(result === "demographic"){
                document.getElementById("mlabel").style.visibility = 'hidden';
                $("#mlabel").hide();
                str = "<option value=\"demographic\">Demographic</option>";
                strTitle = "K-anonymization with Demographic Hierarchies";
            }
            else{
               document.getElementById("mlabel").style.visibility = 'hidden';
               $("#mlabel").hide();
               str = "<option value=\"pFlash\">Parallel k-anonymization</option>";//<option value=\"pFlash\">Parallel Flash</option>;
//               str += "<option value=\"dp\">Differential Privacy</option>";
               strTitle = "Parallel k-anonymization";
               $("#algorithm_title").hide();
               $("#algoselection").show();
            }
            document.getElementById("algoselection").innerHTML = str;
            $("#algorithm_title").html(strTitle);
        },
        error : function(result){
            //alert("getProperAlgorithm: Server problem");
        }
    }); 
}

function getSetValuedColumn(){
    $.ajax({
        url: "/action/getsetvaluedcolumn",
        type: "POST",
        success : function(result) {
           if(!result.includes("none")){
               document.getElementById("setcolumn").value=result
           }
        },
        error : function(result){
            //alert("getProperAlgorithm: Server problem");
        }
    }); 
}


//global variable
var statisticsBarChart = null;



function getBarData(identifiersArr,minArr,maxArr,distinctArr){
    $.ajax({
        url: "/action/getresultsstatisticsqueries",
        type: "POST",
        cache: false,
        async: false,
        data: {identifiers:identifiersArr,min:minArr,max:maxArr,distinct:distinctArr},
        success : function(result) {
            $('#queriesbarchart').on('shown.bs.modal', function (e) {

                var barData = {
                    labels: ["nonOccurrences", "min", "max", "estimated"],
                    datasets: [
                        {
                            label: "Queries",
                            fillColor: "rgba(26,179,148,0.5)",
                            strokeColor: "rgba(26,179,148,0.8)",
                            highlightFill: "rgba(26,179,148,0.75)",
                            highlightStroke: "rgba(26,179,148,1)",
                            data: result
                        }
                    ]
                };

                var barOptions = {
                    scaleBeginAtZero: true,
                    scaleSteps : 10,
                    scaleStepWidth : 100,
                    scaleShowGridLines: true,
                    scaleGridLineColor: "rgba(0,0,0,.05)",
                    scaleGridLineWidth: 1,
                    barShowStroke: true,
                    barStrokeWidth: 2,
                    barValueSpacing: 5,
                    barDatasetSpacing: 1,
                    responsive: true
                }

                var ctx = document.getElementById("barChart").getContext("2d");
                if (statisticsBarChart != null){
                    statisticsBarChart.destroy();
                    statisticsBarChart = null;
//                            ctx.clearRect();
                }
                statisticsBarChart = new Chart(ctx).Bar(barData, barOptions);
//                        statisticsBarChart.create();
            });
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Unable to receive statistics results\n"+error.toString());

            }
            else{
                alert("Unable to receive statistics results");
            }
        }
    });
}


//get results from the statistics queries
function getResultsStatisticsQueries(){
    
     $.ajax({
        url: "/action/getcolumnnamesandtypes",
        type: "POST",
        async: false,
        success : function(result) {
            var identifiersArr = [];
            var minArr = [];
            var maxArr = [];
            var distinctArr =[];
            
            for ( var i = 0 ; i < result.length ; i ++ ){
                var column = document.getElementById(result[i].columnName);
                if($(column).is(':checked')){ 
                    if ( document.getElementById(result[i].columnName+"value") != null){
                        identifiersArr[i] = result[i].columnName;
                        minArr[i] = "null";
                        maxArr[i] = "null";
                        distinctArr[i] = document.getElementById(result[i].columnName+"value").value;     
                    }
                    else{
                        identifiersArr[i] = result[i].columnName;
                        distinctArr[i] = "null";
                        minArr[i] = document.getElementById(result[i].columnName+"min").value;
                        maxArr[i] = document.getElementById(result[i].columnName+"max").value; 
                    }
                }
                else{
                    identifiersArr[i] = "null";
                    minArr[i] = "null";
                    maxArr[i] = "null";
                    distinctArr[i] = "null";
                }
            }
            
            getBarData(identifiersArr,minArr,maxArr,distinctArr) ;
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Unable to receive columns' info\n"+error.toString());
                
            }
            else{
                alert("Unable to receive columns' info");
            }
        }
    }); 
}




/*
function hierarchiesStatistics(){
    $.ajax({
        url: "/action/gethiernamesandlevels",
        type: "POST",
        success : function(result) {
            console.log("result = " + result);
            
     
        },
        error : function(result){
           
            alert("Server problem");
        }
        
    }); 
    
}*/


//delete dataset
function deleteDataset(){
    
    $.ajax({
        url: "/action/deletedataset",
        type: "POST",
        data :{},
        success : function(result) {
            window.location = "mydataset.html";
        },
        error : function(xhr, status, error){
            if(xhr.hasOwnProperty('responseText')){
                console.log(xhr.responseText);
                console.log(status.toString());
                errorHandling(xhr.responseText);
                alert("Unable to delete dataset\n"+error.toString());
                
            }
            else{
                alert("Unable to delete dataset");
            }
        }
    });
}


//delete hierarchy
function deteleHierarchy(){
    
    window.location = "myhier.html";
//    $.ajax({
//        url: "/action/deletehier",
//        type: "POST",
//        data :{},
//        success : function(result) {
//            window.location = "myhier.html";
//        },
//        error : function(result){
//            alert("deteleHierarchy: Server problem");
//        }
//    });
}


//save anonymized rules
function saveAnonumizationRules(){
    
    $.fileDownload('/action/savenonymizationrules')
    .done(function () { alert('File download a success!'); })
    .fail(function () { alert('File download failed!'); });
}


function cleanUserData() {
    $.ajax({
        url: "/action/cleanusedata",
        type: "POST",
        data :{},
        success : function(result) {
            console.log("everything is good");
        },
        error : function(result){
            console.log("cleanUserData: Server problem");
        }
    });
}


String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};
