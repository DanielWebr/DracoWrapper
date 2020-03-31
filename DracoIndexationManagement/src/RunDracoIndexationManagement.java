public class RunDracoIndexationManagement {
    public static void main(String[] args) {
        String fileName = "C:/Users/uzivatel/Documents/skola/zswi/draco/draco-master/testdata/deg_faces.obj";
        FileTypes typ = FileTypes.OBJ;

        DracoIndexationManagement indexation = new DracoIndexationManagement(fileName, typ);
        indexation.addIndexes();
    }
}
