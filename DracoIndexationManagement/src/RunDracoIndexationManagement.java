public class RunDracoIndexationManagement {
    public static void main(String[] args) {
        String fileName = "C:/Users/uzivatel/Documents/skola/zswi/draco/draco-master/testdata/cube_att.ply";

        if(args.length>0 && args[0].length()>0)
            fileName = args[0];

        DracoIndexationManagement indexation = new DracoIndexationManagement(fileName);
        indexation.addIndexes();
    }
}
