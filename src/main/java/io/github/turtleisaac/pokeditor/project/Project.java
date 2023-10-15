//package io.github.turtleisaac.pokeditor.project;
//
//import io.github.turtleisaac.pokeditor.framework.XmlReader;
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.Objects;
//
//public class Project implements Serializable
//{
//    private String name;
//    private File projectPath;
//    private int generation;
//    private Game baseRom;
//    private String baseRomGameCode;
//    private String language;
//    private String program;
//    private String programVersion;
//
//
//    public Project(String name, String path, String program)
//    {
//        this.name= name;
//        projectPath= new File(path);
//        this.program= program;
//    }
//
//    public void setName(String name)
//    {
//        this.name = name;
//    }
//
//    public String getName()
//    {
//        return name;
//    }
//
//    public File getProjectPath()
//    {
//        return projectPath;
//    }
//
//    public void setProjectPath(File projectPath)
//    {
//        this.projectPath = projectPath;
//    }
//
//    public int getGeneration()
//    {
//        return generation;
//    }
//
//    public void setGeneration(int generation)
//    {
//        this.generation = generation;
//    }
//
//    public Game getBaseRom()
//    {
//        return baseRom;
//    }
//
//    public void setBaseRom(Game baseRom)
//    {
//        this.baseRom = baseRom;
//    }
//
//    public String getBaseRomGameCode()
//    {
//        return baseRomGameCode;
//    }
//
//    public void setBaseRomGameCode(String baseRomGameCode)
//    {
//        this.baseRomGameCode = baseRomGameCode;
//    }
//
//    public String getLanguage()
//    {
//        return language;
//    }
//
//    public void setLanguage(String language)
//    {
//        this.language = language;
//    }
//
//    public String getProgram()
//    {
//        return program;
//    }
//
//    public void setProgram(String program)
//    {
//        this.program = program;
//    }
//
//    public String getDataPath()
//    {
//        return new File(getProjectPath().getAbsolutePath() + File.separator + getName() + File.separator + "data").getAbsolutePath();
//    }
//
//    public String getProgramVersion()
//    {
//        return programVersion;
//    }
//
//    public void setProgramVersion(String programVersion)
//    {
//        this.programVersion = programVersion;
//    }
//
//    public String getXml()
//    {
//        String ret= "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
//        ret+= "<" + program.toLowerCase() + ">\n";
//        ret+= "  <name>" + name + "<\\name>\n";
//        ret+= "  <baseromname>" + baseRom + "<\\baseromname>\n";
//        ret+= "  <baseromgamecode>" + baseRomGameCode + "<\\baseromgamecode>\n";
//        ret+= "  <language>" + language.toUpperCase() + "<\\language>\n";
//        ret+= "  <programversion>" + programVersion + "<\\programversion>\n";
//        ret+= "<\\" + program.toLowerCase() + ">";
//
//        return ret;
//    }
//
//    @Override
//    public String toString()
//    {
//        return "Project{" +
//                "name='" + name + '\'' +
//                ", projectPath=" + projectPath +
//                ", generation=" + generation +
//                ", baseRomName='" + baseRom + '\'' +
//                ", baseRomGameCode='" + baseRomGameCode + '\'' +
//                ", language='" + language + '\'' +
//                ", program='" + program + '\'' +
//                ", programVersion='" + programVersion + '\'' +
//                '}';
//    }
//
//    @Override
//    public boolean equals(Object o)
//    {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Project project = (Project) o;
//        return generation == project.generation &&
//                Objects.equals(name, project.name) &&
//                Objects.equals(projectPath, project.projectPath) &&
//                Objects.equals(baseRom, project.baseRom) &&
//                Objects.equals(baseRomGameCode, project.baseRomGameCode) &&
//                Objects.equals(language, project.language) &&
//                Objects.equals(program, project.program) &&
//                Objects.equals(programVersion, project.programVersion);
//    }
//
//    @Override
//    public int hashCode()
//    {
//        return Objects.hash(name, projectPath, generation, baseRom, baseRomGameCode, language, program, programVersion);
//    }
//
//    public static Project readFromXml(String path) throws IOException
//    {
//        XmlReader xmlReader= new XmlReader(path);
//
//        HashMap<String,String> data = xmlReader.readFile();
//
////        System.out.println(data.toString());
//
//        Project project= new Project(data.get("name"),path.substring(0,path.lastIndexOf(File.separator)),data.get("project"));
//        project.setBaseRom(parseBaseRom(data.get("baseromgamecode")));
//        project.setBaseRomGameCode(data.get("baseromgamecode"));
//        project.setLanguage(data.get("language"));
//        project.setProgram(data.get("program"));
//
//        //new
////        String programVersion = data.get("programversion");
////        project.setProgramVersion(Objects.requireNonNullElse(programVersion, PokEditor.versionNumber));
//        project.setProgramVersion(data.get("programversion"));
//
//        return project;
//    }
//
//    public static Game parseBaseRom(String baseRomGameCode)
//    {
//        switch (baseRomGameCode.substring(0,3))
//        {
//            case "ADA":
//                return Game.Diamond;
//
//            case "APA":
//                return Game.Pearl;
//
//            case "CPU":
//                return Game.Platinum;
//
//            case "IPK":
//                return Game.HeartGold;
//
//            case "IPG":
//                return Game.SoulSilver;
//
//            default:
//                throw new RuntimeException("Invalid game");
//        }
//    }
//
//    public String getBaseTitle()
//    {
//        switch (baseRomGameCode.substring(0,3))
//        {
//            case "ADA":
//                return "Pokemon Diamond";
//
//            case "APA":
//                return "Pokemon Pearl";
//
//            case "CPU":
//                return "Pokemon Platinum";
//
//            case "IPK":
//                return "Pokemon HeartGold";
//
//            case "IPG":
//                return "Pokemon SoulSilver";
//
//            default:
//                throw new RuntimeException("Invalid game");
//        }
//    }
//
//    private boolean isDP(String gameCode)
//    {
//        return isPearl(gameCode) || isDiamond(gameCode);
//    }
//
//    private boolean isDiamond(String gameCode)
//    {
//        return gameCode.startsWith("ADA");
//    }
//
//    private boolean isPearl(String gameCode)
//    {
//        return gameCode.startsWith("APA");
//    }
//
//    private boolean isPlatinum(String gameCode)
//    {
//        return gameCode.startsWith("CPU");
//    }
//
//    private boolean isHGSS(String gameCode)
//    {
//        return isHeartGold(gameCode) || isSoulSilver(gameCode);
//    }
//
//    private boolean isHeartGold(String gameCode)
//    {
//        return gameCode.startsWith("IPK");
//    }
//
//    private boolean isSoulSilver(String gameCode)
//    {
//        return gameCode.startsWith("IPG");
//    }
//
//    public static boolean isPrimary(Project project)
//    {
//        return project.getBaseRom() == Game.Platinum || project.getBaseRom() == Game.HeartGold || project.getBaseRom() == Game.SoulSilver;
//    }
//
//    public static boolean isHGSS(Project project)
//    {
//        return project.getBaseRom() == Game.HeartGold || project.getBaseRom() == Game.SoulSilver;
//    }
//
//    public static boolean isDPPT(Project project)
//    {
//        return project.getBaseRom() == Game.Diamond || project.getBaseRom() == Game.Pearl || project.getBaseRom() == Game.Platinum;
//    }
//
//    public static boolean isPlatinum(Project project)
//    {
//        return project.getBaseRom() == Game.Platinum;
//    }
//
//    public static void saveProject(Project project, String path) throws IOException
//    {
//        BufferedWriter writer= new BufferedWriter(new FileWriter(path));
//        writer.write(project.getXml());
//        writer.close();
//    }
//}
