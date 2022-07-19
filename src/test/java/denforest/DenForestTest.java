package denforest;

import example.Denforest_optimized_test;
import example.Denforest_test;
import org.junit.Assert.*;
import org.junit.Test;

public class DenForestTest {

    @Test
    public void test1(){
        System.out.println("[Test1] Run DenForest Test");

        Denforest_test test = new Denforest_test();

        System.out.println("[Test1-1]");

        String[] args1 = {"./sample_dataset","1", "5", "0.002", "100000", "5000", "5"};
        test.run(args1);

        System.out.println("[Test1-2]");

        String[] args2 = {"./sample_dataset","2", "5", "0.002", "50000", "5000", "5"};
        test.run(args2);

        System.out.println("[Test1-3]");

        String[] args3 = {"./sample_dataset","3", "5", "2", "50000", "5000", "5"};
        test.run(args3);

        System.out.println("[Test1-4]");

        String[] args4 = {"./sample_dataset","4", "5", "0.3", "50000", "5000", "5"};
        test.run(args4);

        System.out.println("[Test1] Run DenForest Complete!");

    }

    @Test
    public void test2(){

        System.out.println("[Test2] Run DenForestOpt Test");

        Denforest_optimized_test test = new Denforest_optimized_test();
        System.out.println("[Test2-1]");

        String[] args1 = {"./sample_dataset","1", "5", "0.002", "100000", "5000", "5"};
        test.run(args1);

        System.out.println("[Test2-2]");

        String[] args2 = {"./sample_dataset","2", "5", "0.002", "50000", "5000", "5"};
        test.run(args2);

        System.out.println("[Test2-3]");

        String[] args3 = {"./sample_dataset","3", "5", "2", "50000", "5000", "5"};
        test.run(args3);

        System.out.println("[Test2-4]");

        String[] args4 = {"./sample_dataset","4", "5", "0.3", "50000", "5000", "5"};
        test.run(args4);
        System.out.println("[Test2] Run DenForestOpt Complete!");

    }
}