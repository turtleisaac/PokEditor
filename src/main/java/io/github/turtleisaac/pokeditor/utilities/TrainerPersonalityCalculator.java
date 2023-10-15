package io.github.turtleisaac.pokeditor.utilities;

public class TrainerPersonalityCalculator
{
    private static final String[] natures= {"Hardy","Lonely","Brave","Adamant","Naughty","Bold","Docile","Relaxed","Impish","Lax","Timid","Hasty","Serious","Jolly","Naive","Modest","Mild","Quiet","Bashful","Rash","Calm","Gentle","Sassy","Careful","Quirky"};

    public static void main(String[] args)
    {
        int trainerIdx= 320;
        int classIdx= 79;
        boolean male= true;
        int speciesIdx= 466;
        int level= 50;
        int difficultyValue= 2500;

        int pid= generatePid(trainerIdx,classIdx,male,speciesIdx,level,difficultyValue, 0, false);

        paraSet(difficultyValue*31.0/255, pid);

//        System.out.println(calculateShiny(pid,30055,31231,128));
    }

    public static int bruteForcePid(int targetPid, int trainerIdx, int trainerClassIdx, boolean trainerClassMale, int speciesIdx, int level)
    {
        for(int i= 0; i < 65535; i++)
        {
            if(generatePid(trainerIdx,trainerClassIdx,trainerClassMale,speciesIdx,level,i, 0, false) == targetPid)
            {
                return i;
            }
        }
        return -1;
    }


    public static int generatePid(int trainerIdx, int trainerClassIdx, boolean trainerClassMale, int speciesIdx, int level, int difficultyValue, int abilityValue, boolean abilityFunctional)
    {
        long rnd= trainerIdx+speciesIdx+level+difficultyValue;
        setRandom(rnd);
        for(;trainerClassIdx > 0; trainerClassIdx--)
        {
            rnd= random();
        }
        rnd= (rnd >> 16) & 0xffff;
        rnd*= 256;
        rnd+= trainerClassMale ? 136 : 120;

        if(abilityFunctional)
        {
            rnd+= abilityValue;
        }

        //TODO look at how the ability value stuff functions

        String result= Long.toHexString((int)rnd);
//        System.out.println("PID: 0x00" + (result.length() == 6 ? result : "0" + result));
//        System.out.println("Nature: " + natures[(int)(rnd%100)%25] + "\n");

        return (int)rnd;
    }

    public static long rndFlagCall()
    {
        return (random() | (random() << 16));
//        return ((random() << 16) | random());
    }

    public static long idSetCall(long id, int pid)
    {
        long tid;
        long sid;

        do
        {
            id= rndFlagCall();
            tid= (id >> 16) & 0xffff;
            sid= id & 0xffff;
            System.out.println("TID: " + tid);
            System.out.println("SID: " + sid);
        }
        while(calculateShiny(pid, tid, sid,8));

        return id;
    }

    public static void ivCalculation()
    {
        long i= random();
        int j= (int) ((i & (0x001f)));
        System.out.println("    Hp IV: " + j);
        j= (int) (i&(0x001f << 5))>> 5;
        System.out.println("    Atk IV: " + j);
        j= (int) (i&(0x001f << 10))>> 10;
        System.out.println("    Def IV: " + j);
        i= random();
        j= (int) ((i & (0x001f)));
        System.out.println("    Speed IV: " + j);
        j= (int) (i&(0x001f << 5))>> 5;
        System.out.println("    SpAtk IV: " + j);
        j= (int) (i&(0x001f << 10))>> 10;
        System.out.println("    SpDef IV: " + j);
    }

    public static void paraSet(double difficulty, int pid)
    {
//        long localSeed= seed;
//
//
//        long id;
//        long tid;
//        long sid;
//        seed= localSeed;
//        int x;
//        for(x= 0; x < i; x++)
//        {
//            id= rndFlagCall();
//            tid= (id >> 16) & 0xffff;
//            sid= id & 0xffff;
//            if(tid == 55963)
//            {
//                System.out.println(x + ": " + tid + ", " + sid);
//            }
//        }




//        pid= (int) rndFlagCall();

        long id= idSetCall(0, pid);

        if(difficulty <= 31)
        {
            System.out.println("    Hp IV: " + (int) difficulty);
            System.out.println("    Atk IV: " + (int) difficulty);
            System.out.println("    Def IV: " + (int) difficulty);
            System.out.println("    Spe IV: " + (int) difficulty);
            System.out.println("    SpAtk IV: " + (int) difficulty);
            System.out.println("    SpDef IV: " + (int) difficulty);

        }
        else
        {
            ivCalculation();
        }

    }

    public static boolean calculateShiny(int pid, long tid, long sid, int rateDenominator)
    {
        int p1= (pid >> 16) & 0xffff;
//        System.out.println("p1: " + Integer.toHexString(p1));
        int p2= pid & 0xffff;
//        System.out.println("p2: " + Integer.toHexString(p2));

        long shiny= tid ^ sid ^ p1 ^ p2;
        System.out.println("TID= " + tid + ", Shiny Value: " + shiny);

        return shiny < rateDenominator;
    }


    /**
     * Random function code
     */

    private static long seed;

    public static void setRandom(long newSeed)
    {
        seed= newSeed;
    }

    public static long random()
    {
        long result= 0x41c64e6d * seed + 0x6073;
        seed= result & 0xffffffffL;
        return result;
    }
}
