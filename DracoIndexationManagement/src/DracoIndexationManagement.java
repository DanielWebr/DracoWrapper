import java.io.*;

public class DracoIndexationManagement {
    public FileTypes fileType;
    public File file;

    public DracoIndexationManagement(String pathToFile) {
        this.file = new File(pathToFile);

        if(getFileExtension(this.file).equalsIgnoreCase(".obj"))
            this.fileType = FileTypes.OBJ;
        else if(getFileExtension(this.file).equalsIgnoreCase(".ply"))
            this.fileType = FileTypes.PLY;
    }

    public static String getFileExtension(File file) {
        String extension = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }

        return extension;

    }

    public static String getFileName(File file){
        String fileName = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.substring(0,name.indexOf("."));
            }
        } catch (Exception e) {
            fileName = "";
        }

        return fileName;

    }

    public void addIndexes(){
        String newFile = "";
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(this.file);
        }catch (Exception e){
            System.out.println(e);
        }
        
        if(this.fileType == FileTypes.OBJ){
            try{

                String line = null;
                int index = 0;

                BufferedReader bufferedReader = new BufferedReader(fileReader);
                while((line = bufferedReader.readLine()) != null) {
                    //System.out.println(line);

                    if(line.length()>1 && line.substring(0,2).trim().equals("v")){
                        newFile += line+" "+index+"\n";
                        index++;
                    }else{
                        newFile += line+"\n";
                    }
                }

                bufferedReader.close();

            }catch (Exception e){
                System.out.println(e);
            }

            //System.out.println(newFile);
        }else if(this.fileType == FileTypes.PLY){
            try{

                String line = null;
                int index = 0;
                int stopLine = 0;
                int startLine = 0;
                int currentLine = 1;
                int numberOfVertexes = 0;

                BufferedReader bufferedReader = new BufferedReader(fileReader);
                while((line = bufferedReader.readLine()) != null) {
                    //System.out.println(line);

                    if(line.startsWith("element vertex")){
                        String numberOfVertexesS = line.substring(line.lastIndexOf(" ")+1);
                        numberOfVertexes = Integer.parseInt(numberOfVertexesS);
                    }

                    if(line.startsWith("end_header")){
                        startLine = currentLine + 1;
                        stopLine = numberOfVertexes + startLine;
                    }

                    if(startLine>0 && startLine<=currentLine && stopLine>0 && currentLine<stopLine ){
                        newFile += line+" "+index+"\n";
                        index++;
                    }else{
                        newFile += line+"\n";
                    }

                    currentLine++;
                }

                bufferedReader.close();

            }catch (Exception e){
                System.out.println(e);
            }
        }

        try{
            FileWriter fileWriter = new FileWriter(this.file.getParent()+"/"+getFileName(this.file)+"_index"+getFileExtension(this.file));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(newFile);
            bufferedWriter.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }


    public FileTypes getFileType() {
        return fileType;
    }

    public void setFileType(FileTypes fileType) {
        this.fileType = fileType;
    }

}
