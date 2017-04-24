/*
 * Copyright 2013,2014 BioASQ project: FP7/2007-2013, ICT-2011.4.4(d), 
 *  Intelligent Information Management, 
 *  Targeted Competition Framework grant agreement n° 318652.
 *  www: http://www.bioasq.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 * @author Ioannis Partalas
 */
package evaluation;

import data.Question;
import data.Task1bData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class EvaluatorTask1b {
    
    Task1bData goldenData;
    Task1bData systemResp;
    double epsilon=0.00001;
    // The same as in Task1bData
    int VERSION_OF_CHALLENGE=5; // we use this to have modified versions of the measures for different BioASQ years
    // Use version 2 for BioASQ1&2, version 3 for BioASQ3&4, version 5 since BioASQ5
    public static final int BIOASQ2=2,BIOASQ3=3,BIOASQ5=5;
    boolean verbosity = false;
    
    
    
    public EvaluatorTask1b(String golden, String system,int version)
    {
        goldenData = new Task1bData(version, true);
        systemResp = new Task1bData(version, false);
        try {
            goldenData.readData(golden);
            systemResp.readData(system);
        } catch (IOException ex) {
            Logger.getLogger(EvaluatorTask1b.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    public void EvaluatePhaseA()
    {
        ArrayList<QuestionAnswerEvaluator> qevalArray = new ArrayList<QuestionAnswerEvaluator>();
        
    //    System.out.println("Golden data: "+goldenData.numQuestions());
    //    System.out.println("System replies: "+systemResp.numQuestions());
        
        for(int i=0;i<goldenData.numQuestions();i++)
        {
            Question gold = goldenData.getQuestion(i);
            Question resp = systemResp.getQuestion(gold.getId());
            if(resp==null)
                continue;
            
            QuestionAnswerEvaluator qeval =new QuestionAnswerEvaluator(gold.getId(),this.VERSION_OF_CHALLENGE,true);
            qeval.calculateMeasuresForPair(gold, resp);
            qevalArray.add(qeval);
        }
        
        System.out.print(MeanPrecisionConcepts(qevalArray)+" "+
                MeanRecallConcepts(qevalArray)+" "+MeanF1Concepts(qevalArray)+" "+MapConcepts(qevalArray)
                +" "+GMapConcepts(qevalArray)+" ");

        System.out.print(MeanPrecisionArticles(qevalArray)+" "+
                MeanRecallArticles(qevalArray)+" "+MeanF1Articles(qevalArray)+" "+MapDocuments(qevalArray)
                +" "+GMapDocuments(qevalArray)+" ");
        
        System.out.print(MeanPrecisionSnippets(qevalArray)+" "+
                MeanRecallSnippets(qevalArray)+" "+MeanF1Snippets(qevalArray)+" "+MapSnippets(qevalArray)
                +" "+GMapSnippets(qevalArray)+" ");
        
        System.out.print(MeanPrecisionTriples(qevalArray)+" "+
                MeanRecallTriples(qevalArray)+" "+MeanF1Triples(qevalArray)+" "+MapTriples(qevalArray)
                +" "+GMapTriples(qevalArray));
        
        if(!this.verbosity){
        System.out.println("\nMAP concepts: "+MapConcepts(qevalArray));
        System.out.println("MAP documents: "+MapDocuments(qevalArray));
        System.out.println("MAP triples: "+MapTriples(qevalArray));
        System.out.println("MAP snippets: "+GMapSnippets(qevalArray));
        System.out.println("GMAP concepts: "+GMapConcepts(qevalArray));
        System.out.println("GMAP documents: "+GMapDocuments(qevalArray));
        System.out.println("GMAP triples: "+GMapTriples(qevalArray));
        System.out.println("GMAP snippets: "+GMapSnippets(qevalArray));
        System.out.println("F1 snippets: "+F1Snippets(qevalArray));
        }
    }
    
    
    public void EvaluatePhaseB()
    {
        ArrayList<QuestionAnswerEvaluator> qevalArray = new ArrayList<QuestionAnswerEvaluator>();
        
        for(int i=0;i<goldenData.numQuestions();i++)
        {
            Question gold = goldenData.getQuestion(i);
            Question resp = systemResp.getQuestion(gold.getId());
            
            if(resp==null) continue;
             
             QuestionAnswerEvaluator qeval =new QuestionAnswerEvaluator(gold.getId(), 
                     gold.getType(),this.VERSION_OF_CHALLENGE);
             qeval.calculatePhaseBMeasuresForPair(gold, resp);
             qevalArray.add(qeval);

       
        }
        
        System.out.print(AccuracyExactAnswersYesNo(qevalArray)+" "+strictAccuracy(qevalArray)+" "
                +lenientAccuracy(qevalArray)+" "+meanReciprocalRank(qevalArray)+" "
                +listPrecision(qevalArray) +" "+listRecall(qevalArray)+" "+listF1(qevalArray));
        
        if(this.verbosity){
        System.out.println("\nYesNo accyracy: "+AccuracyExactAnswersYesNo(qevalArray));
        System.out.println("Strict accuracy: "+strictAccuracy(qevalArray));
        System.out.println("Lenient accuracy: "+lenientAccuracy(qevalArray));
        System.out.println("MRR: "+meanReciprocalRank(qevalArray));
        System.out.println("List f1: "+listF1(qevalArray));
        }
    }
            
    
    public double AccuracyExactAnswersYesNo(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        int k=0;
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).getQuestion_type()==Question.YESNO)
            {
                m+= qeval.get(i).getAccuracyYesNo();
                k++;
            }
        }
        
	if(k==0)
	    return 0;
        return m/k;
    }
    
    public double strictAccuracy(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        int k=0;
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).getQuestion_type()==Question.FACTOID)
            {
                m+= qeval.get(i).getStrictAccuracy();
                k++;
            }
        }
        
	if(k==0)
	    return 0;
        return m/k;
    }
    
    public double lenientAccuracy(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        int k=0;
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).getQuestion_type()==Question.FACTOID)
            {
                m+= qeval.get(i).getLenientAccuracy();
                k++;
            }
        }
        
	if(k==0)
	    return 0;
        return m/k;
    }
    
    public double meanReciprocalRank(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        int k=0;
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).getQuestion_type()==Question.FACTOID)
            {
                m+= qeval.get(i).getMRR();
                k++;
            }
        }
        
	if(k==0)
	    return 0;
        return m/k;
    }

    public double listPrecision(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        int k=0;
        double pre=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).getQuestion_type()==Question.LIST)
            {
	   if(Double.isNaN(qeval.get(i).getPrecisionEA()))
		   pre+=0;
	   else
                pre+= qeval.get(i).getPrecisionEA();
                k++;
            }
        }
        
	if(k==0)
	    return 0;
        return pre/k;
    }


    public double listRecall(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        int k=0;
        double recall=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).getQuestion_type()==Question.LIST)
            {
                
	   if(Double.isNaN(qeval.get(i).getRecallEA()))
		   recall+=0;
	   else
                recall+= qeval.get(i).getRecallEA();
                
                k++;
            }
        }
        
	if(k==0)
	    return 0;
        return recall/k;
    }

        
    public double listF1(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        int k=0;
        double f1=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).getQuestion_type()==Question.LIST)
            {
               
		  //System.out.println(qeval.get(i).getF1EA());
		  if(Double.isNaN(qeval.get(i).getF1EA()))
			  f1+=0;
		  else
                	f1+=qeval.get(i).getF1EA();
                k++;
            }
        }
        
	if(k==0)
	    return 0;
        return f1/k;
    }

    
    public double MapConcepts(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int sz=0;
        
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).hasQuestionConcepts()){
	   if(Double.isNaN(qeval.get(i).getAveragePrecisionConcepts()))
		   m+=0;
	   else
                        m+=qeval.get(i).getAveragePrecisionConcepts();
                sz++;
            }
        }
       if(sz==0)
	      return 0; 
        return m/sz;
    }

    public double MeanPrecisionConcepts(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int sz=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).hasQuestionConcepts()){
	   if(Double.isNaN(qeval.get(i).getConceptsPrecision()))
		   m+=0;
	   else
           m+=qeval.get(i).getConceptsPrecision();
           sz++;
            }
        }
       if(sz==0)
	      return 0; 
        return m/sz;
    }

    
    public double MeanRecallConcepts(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int sz=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).hasQuestionConcepts()){
	   if(Double.isNaN(qeval.get(i).getConceptsRecall()))
		   m+=0;
	   else
                            m+=qeval.get(i).getConceptsRecall();
           sz++;
            }
        }
       if(sz==0)
	   return 0; 
       
        return m/sz;
    }
    
    
    public double MeanF1Concepts(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int sz=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).hasQuestionConcepts()){
		 if(Double.isNaN(qeval.get(i).getConceptsF1()))
			      m+=0;
	         else
	           m+=qeval.get(i).getConceptsF1();
           sz++;
            }
        }
        if(sz==0)
		return 0;
        return m/sz;
    }
    
    
    public double MeanPrecisionArticles(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int sz=0;
        for(int i=0;i<qeval.size();i++)
        {
	   if(Double.isNaN(qeval.get(i).getArticlesPrecision()))
		   m+=0;
	   else
                                m+=qeval.get(i).getArticlesPrecision();
           sz++;
        }
         if(sz==0)
		return 0;
        return m/sz;
    }

    
    public double MeanRecallArticles(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int sz=0;
        for(int i=0;i<qeval.size();i++)
        {
	   if(Double.isNaN(qeval.get(i).getArticlesRecall()))
		   m+=0;
	   else
                                    m+=qeval.get(i).getArticlesRecall();
           sz++;
        }
        if(sz==0)
            return 0;
        return m/qeval.size();
    }
    
    
    public double MeanF1Articles(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
		if(Double.isNaN(qeval.get(i).getArticlesF1()))
			m+=0;
		else
           		m+=qeval.get(i).getArticlesF1();
        }
        
        return m/qeval.size();
    }
    
    
        public double MeanPrecisionSnippets(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
	   if(Double.isNaN(qeval.get(i).getSnippetsPrecision())){
		   m+=0;System.out.println("isnan");}
	   else
           m+=qeval.get(i).getSnippetsPrecision();
        }
        
        return m/qeval.size();
    }

    
    public double MeanRecallSnippets(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
	   if(Double.isNaN(qeval.get(i).getSnippetsRecall())){
		   m+=0;System.out.println("isnan");
           }
	   else
           m+=qeval.get(i).getSnippetsRecall();
        }
        
        return m/qeval.size();
    }
    
    
    public double MeanF1Snippets(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
		if(Double.isNaN(qeval.get(i).getSnippetsF1()))
			m+=0;
		else
           		m+=qeval.get(i).getSnippetsF1();
        }
        
        return m/qeval.size();
    }
    
    
    public double MeanPrecisionTriples(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int num=0;
        for(int i=0;i<qeval.size();i++)
        {
             if(qeval.get(i).is_triple){
	   if(Double.isNaN(qeval.get(i).getTriplesPrecision()))
		   m+=0;
	   else
                m+=qeval.get(i).getTriplesPrecision();
                num++;
             }
        }
        
        if(num==0)
            return 0;
        
        return m/(double)num;
    }

    
    public double MeanRecallTriples(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int num=0;
        for(int i=0;i<qeval.size();i++)
        {
             if(qeval.get(i).is_triple){
	   if(Double.isNaN(qeval.get(i).getTriplesRecall()))
		   m+=0;
	   else
                m+=qeval.get(i).getTriplesRecall();
                num++;
             }
        }
           if(num==0)
            return 0;
        return m/(double)num;
    }
    
    
    public double MeanF1Triples(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int num=0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).is_triple){
		    if(Double.isNaN(qeval.get(i).getTriplesF1()))
			    m+=0;
		    else
                m+=qeval.get(i).getTriplesF1();
                num++;
            }
        }
           if(num==0)
            return 0;
        return m/(double)num;
    }
    
    
    public double MapDocuments(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
	   if(Double.isNaN(qeval.get(i).getAveragePrecisionDocuments()))
		   m+=0;
	   else
            m+=qeval.get(i).getAveragePrecisionDocuments();
        }
        
        return m/qeval.size();
    }

    public double MapTriples(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int num = 0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).is_triple){
	   if(Double.isNaN(qeval.get(i).getAveragePrecisionTriples()))
		   m+=0;
	   else
            m+=qeval.get(i).getAveragePrecisionTriples();
            num++;
            }
        }
        if(num==0)
            return 0;
        return m/num;
    }

    
    public double MapSnippets(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {

	   if(Double.isNaN(qeval.get(i).getAveragePrecisionSnippets()))
		   m+=0;
	   else
                                    m+=qeval.get(i).getAveragePrecisionSnippets();
        }
       
        return m/qeval.size();
    }

    
    
       public double GMapConcepts(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int sz=0;
        
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).hasQuestionConcepts())
            {
	   if(Double.isNaN(qeval.get(i).getAveragePrecisionConcepts()))
		   m+=0;
	   else
                            m+=Math.log(qeval.get(i).getAveragePrecisionConcepts() + epsilon);
                sz++;
            }
        }
    
    if(sz==0)
    return 0;	    
        return Math.exp(m/sz);
    }

    
    public double GMapDocuments(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m = 0;
        double k=0;
        for (int i = 0; i < qeval.size(); i++) {
            /*if(qeval.get(i).getAveragePrecisionDocuments()==0.0)
            {
            System.out.println(qeval.get(i).getQuestionID());	
            }
            System.out.println(qeval.get(i).getAveragePrecisionDocuments());*/

            if (Double.isNaN(qeval.get(i).getAveragePrecisionDocuments())) {
                m += Math.log(epsilon);
            } else {
                m += Math.log(qeval.get(i).getAveragePrecisionDocuments() + epsilon);
            }
        }
        
        return Math.exp(m/qeval.size());
    }

    public double GMapTriples(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        int num = 0;
        for(int i=0;i<qeval.size();i++)
        {
            if(qeval.get(i).is_triple){
            m+=Math.log(qeval.get(i).getAveragePrecisionTriples()+epsilon);
            num++;
            }
        }
           if(num==0)
            return 0;
        return Math.exp(m/num);
    }

    
    public double GMapSnippets(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {

	   if(Double.isNaN(qeval.get(i).getAveragePrecisionSnippets()))
		   m+=0;
	   else
            m+=Math.log(qeval.get(i).getAveragePrecisionSnippets()+epsilon);
        }
        
        if(Double.isNaN(m))
            return 0;
        
        if(m==0)
            return 0;
        
        return Math.exp(m/qeval.size());
    }

    
    public double F1Snippets(ArrayList<QuestionAnswerEvaluator> qeval)
    {
        double m=0;
        for(int i=0;i<qeval.size();i++)
        {
            m+=qeval.get(i).getF1Snippets();
        }
        
        return m/qeval.size();
    }
    
    public TreeMap loadPubMedCentralDocs(String file)
    {
        TreeMap pubmeddocs = new TreeMap();
        
        BufferedReader bf = null;
        try {
            bf=   new BufferedReader(new FileReader(file));
            String line;
            int num=0;
            while((line=bf.readLine())!=null)
            {
                pubmeddocs.put(line, ++num);
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(EvaluatorTask1b.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return pubmeddocs;
        
    }

    private static Options createOptions()
    {
        Options opt = new Options();
        
        opt.addOption("e", true, "edition of BioASA challenge");
        opt.addOption("phaseA",false,"phase A of Task B");
        opt.addOption("phaseB",false,"phase B of Task B");
        opt.addOption("verbose",false,"verbose output");
        
        return opt;
    }
    
    public void setVERSION_OF_CHALLENGE(int VERSION_OF_CHALLENGE) {
        this.VERSION_OF_CHALLENGE = VERSION_OF_CHALLENGE;
    }

    public void setVerbosity(boolean verbosity) {
        this.verbosity = verbosity;
    }
    
    
    
    private static void usage()
    {
               System.out.println("Usage: -phaseX [-e version] [-verbose] goldenfile systemfile");
               System.out.println("where X can be either A or B for the corresponding phases");
               System.out.println("and year refers to the version of the challenge. It can"
                       + "be 2, 3 or 5 (this argument is optional - default value is 2)");
    }
    
    public static void main(String args[])
    {
           
        Options opt = EvaluatorTask1b.createOptions();

        CommandLineParser parser = new  PosixParser();
        

        try {
            CommandLine line = parser.parse(opt, args);
            String e;
            EvaluatorTask1b eval;
            
            if (!line.hasOption("phaseA") && !line.hasOption("phaseB")) {
                EvaluatorTask1b.usage();
                System.exit(0);
            }
            
            if (line.hasOption("e")) {
                e = line.getOptionValue("e");
                if (e == null) {
                    EvaluatorTask1b.usage();
                    System.exit(0);
                }
            
                eval = new EvaluatorTask1b(args[3], args[4],Integer.parseInt(e));
                eval.setVERSION_OF_CHALLENGE(Integer.parseInt(e));
            } else {
                eval = new EvaluatorTask1b(args[1], args[2],EvaluatorTask1b.BIOASQ2);
                eval.setVERSION_OF_CHALLENGE(EvaluatorTask1b.BIOASQ2);
            }

            if(line.hasOption("verbose"))
            {
                eval.setVerbosity(true);
            }

            
            if (line.hasOption("phaseA")) {
                eval.EvaluatePhaseA();
            }
            if (line.hasOption("phaseB")) {
                eval.EvaluatePhaseB();
            }
            

        } catch (ParseException ex) {
            Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
