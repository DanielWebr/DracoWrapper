import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class DracoIndexationManagement {
    public String pathToFile;
    public String fileName;
    public FileTypes fileType;
    public FileReader fileReader;

    public DracoIndexationManagement(String pathToFile, FileTypes fileType){
        this.pathToFile = pathToFile;
        this.fileType = fileType;

        

        try{
            this.fileReader = new FileReader(this.fileName);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void addIndexes(){
        String newFile = "";

        if(this.fileType == FileTypes.OBJ){
            try{

                String line = null;
                int index = 0;

                BufferedReader bufferedReader = new BufferedReader(fileReader);
                while((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);

                    if(index>0)
                        newFile += "\n";

                    if(line.length()>1 && line.substring(0,2).trim().equals("v")){
                        newFile += line+" "+index;
                    }else{
                        newFile += line;
                    }
                    index++;
                }

                bufferedReader.close();

            }catch (Exception e){
                System.out.println(e);
            }

            System.out.println(newFile);
        }else if(this.fileType == FileTypes.PLY){

        }

        try{
            FileWriter fileWriter = new FileWriter(this.fileName+"_index");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(newFile);
            bufferedWriter.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileTypes getFileType() {
        return fileType;
    }

    public void setFileType(FileTypes fileType) {
        this.fileType = fileType;
    }

}
