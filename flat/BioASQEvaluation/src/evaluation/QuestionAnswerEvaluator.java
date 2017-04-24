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

import data.CalculatedMeasures;
import data.ExactAnswer;
import data.Question;
import data.Snippet;
import java.util.ArrayList;

public class QuestionAnswerEvaluator {
    
    CalculatedMeasures concepts;
    CalculatedMeasures articles;
    CalculatedMeasures triples;
    CalculatedMeasures snippets;
    
    CalculatedMeasures exact_answers;
    
    String question_id;
    int question_type;
    Boolean is_triple=false; 
    Boolean has_concepts=false;
    int VERSION_OF_CHALLENGE;  // Use version 2 for BioASQ1&2, version 3 for BioASQ3&4, version 5 since BioASQ5

    
    public QuestionAnswerEvaluator(String id,int version,boolean fl)
    {
        concepts= new CalculatedMeasures();
        articles=new CalculatedMeasures();
        triples=new CalculatedMeasures();
        snippets=new CalculatedMeasures();
        question_id = id;
        VERSION_OF_CHALLENGE = version;
    }
    
    public QuestionAnswerEvaluator(String id,int qt,int version)
    {
        exact_answers = new CalculatedMeasures();
        question_id = id;
        question_type = qt;
        VERSION_OF_CHALLENGE = version;
    }
   
   public String  getQuestionID()
   {
	   return question_id;
   } 
    
    public void calculatePhaseBMeasuresForPair(Question golden,Question response)
    {
        if(question_type == Question.FACTOID)
        {
            if(this.VERSION_OF_CHALLENGE == evaluation.EvaluatorTask1b.BIOASQ2){
                strictAccuracy(golden.getExact_answer(),response.getExact_answer(),exact_answers);
                lenientAccuracy(golden.getExact_answer(),response.getExact_answer(),exact_answers);
                meanReciprocalRank(golden.getExact_answer(),response.getExact_answer(),exact_answers);
            }
            else if(this.VERSION_OF_CHALLENGE==evaluation.EvaluatorTask1b.BIOASQ3 || this.VERSION_OF_CHALLENGE==evaluation.EvaluatorTask1b.BIOASQ5)
            {
                strictAccuracyForLists(golden.getExact_answer(),response.getExact_answer(),exact_answers);
                lenientAccuracyForLists(golden.getExact_answer(),response.getExact_answer(),exact_answers);
                meanReciprocalRankForLists(golden.getExact_answer(),response.getExact_answer(),exact_answers);
            }
        }
        else if(question_type == Question.LIST)
        {
            //try{
            //calculatePRF(golden.getExact_answer().getAnswers(),response.getExact_answer().getAnswers(),exact_answers);
              calculatePRFforListQuestions(golden.getExact_answer(),response.getExact_answer(),exact_answers);
            //}catch(Exception ex){System.out.println(golden.getId());}
        }
        else if(question_type == Question.YESNO)
        {
            accuracyYesNo(golden.getExact_answer(),response.getExact_answer(),exact_answers);
        }
    }
    
    public void calculateMeasuresForPair(Question golden,Question response)
    {try{
        if(golden.getConcepts().size()>0 && !response.getConcepts().isEmpty())
        {
            calculatePRF(golden.getConcepts(), response.getConcepts(), concepts);
            has_concepts = true;
        }}
    catch(Exception ex){System.out.println(ex.toString());System.out.println(golden.getId());}
        
        
        calculatePRF(golden.getDocuments(), response.getDocuments(), articles);
        if(!golden.getTriples().isEmpty())
        {
            calculatePRF(golden.getTriples(), response.getTriples(), triples);
            is_triple = true;
        }
        
        concatenateSnippets(golden.getSnippets());
        concatenateSnippets(response.getSnippets());
        
        calculatePRForSnippets(golden.getSnippets(), response.getSnippets(),snippets);
        
        
        calculateAveragePrecision(golden.getConcepts(), response.getConcepts(), concepts);
        calculateAveragePrecision(golden.getDocuments(), response.getDocuments(), articles);
        
        if(!golden.getTriples().isEmpty())
            calculateAveragePrecision(golden.getTriples(), response.getTriples(), triples);     
        
        calculateAveragePrecisionSnippets(golden.getSnippets(), response.getSnippets(), snippets);
    }
    /**
     * 
     * @param listGolden
     * @param listResponses
     * @param cm
     * 
     */
    
    public void calculatePRForSnippets(ArrayList<Snippet> listGolden, ArrayList<Snippet> listResponses, CalculatedMeasures cm)       
    {
        
        if(listResponses.isEmpty())
        {
            return;
        }
        
        int resp_size=0;
        int total_overlap=0;
        int g_size=0;
        int skippeddocs=0;
        
        for(int i=0;i<listResponses.size();i++)
        {
            
            Snippet sn = listResponses.get(i);
            /*if(listPubMedCentral.containsKey(sn.getDocumentOnlyID())){ // skip the documents that come from PubMedCentral
                 skippeddocs++;   continue;
            }*/
            
         //   if(sn.getSize()<0)
          //  {System.out.println(this.question_id);System.out.println(skippeddocs);System.exit(0);
          //  }
            resp_size += sn.getSize();
            int docsfound=0;
            for(int j=0;j<listGolden.size();j++)
            {
                Snippet g = listGolden.get(j);
               // if(listPubMedCentral.containsKey(g.getDocumentOnlyID())) // skip the documents that come from PubMedCentral
               //     continue;
                if(sn.getDocumentOnlyID().equals(g.getDocumentOnlyID())) // we can have more than one snippet per document and per paragraph
                {docsfound++;
                    total_overlap += sn.overlap(g);
                }
            }
         //   System.out.println("Docs found: "+docsfound +" question: "+this.question_id +" doc: "+sn.getDocument());
           // System.out.println("Total overlap :" + total_overlap);
        }
        
        for(int j=0;j<listGolden.size();j++)
         {
            Snippet g = listGolden.get(j);
            //  if(listPubMedCentral.containsKey(g.getDocumentOnlyID())) // skip the documents that come from PubMedCentral
             //       continue;
            g_size+=g.getSize();
         }
        
     //   System.out.println("Total overlap :" + total_overlap +" Resp size: "+resp_size +" gold: "+g_size);
        if(resp_size != 0)
        cm.setPrecision((double)total_overlap/((double)resp_size));
        if(g_size!=0)
        cm.setRecall((double)total_overlap/(double)g_size);
        if(cm.getPrecision()!=0 || cm.getRecall()!=0)
           cm.setFmeasure(2*cm.getPrecision()*cm.getRecall()/(cm.getPrecision()+cm.getRecall()));
    }
    
    public void calculatePRF(ArrayList listGolden, ArrayList listResponses, CalculatedMeasures cm)
    {
        double tp=0,fp=0,fn=0;
        
      
        
        
        if(listResponses.isEmpty())
        {
            return;
        }
        
        for(int i=0;i<listResponses.size();i++)
        {
            Object item = listResponses.get(i);
            if(listGolden.contains(item))
                tp++;
            else
            {
                fp++;
            }
        }

        for(int i=0;i<listGolden.size();i++)
        {
            Object item = listGolden.get(i);
            if(!listResponses.contains(item))
                fn++;
        }

      
        cm.setPrecision(tp/(tp+fp));
      
        if((fn+tp)!=0)
            cm.setRecall(tp/(tp+fn));
        
        if(cm.getPrecision()!=0 && cm.getRecall()!=0)
            cm.setFmeasure(2*cm.getPrecision()*cm.getRecall()/(cm.getPrecision()+cm.getRecall()));
    }
    
    
    public void calculatePRFforListQuestions(ExactAnswer golden,ExactAnswer  response, CalculatedMeasures cm)
    {
        double tp=0,fp=0,fn=0;
        
      
        
        
        if(response==null||response.getLists().isEmpty())
        {
            return;
        }
        
        for(int i=0;i<response.getLists().size();i++)
        {
	    // check if the answer has a synonym
            if(golden.containsAnswerSynonym(response.getLists().get(i),true))
            {
                tp++;
            }
            else
            {
                fp++;
            }
        }

        for(int i=0;i<golden.getLists().size();i++)
        {
            if(!response.containsAnswerSynonym(golden.getLists().get(i),true))
                fn++;
        }

        //System.out.println("TP: "+tp+"  FP: "+fp +" FN: "+fn);
        cm.setPrecision(tp/(tp+fp));
        if((fn+tp)!=0)
            cm.setRecall(tp/(tp+fn));
        
        if(cm.getPrecision()!=0 && cm.getRecall()!=0)
            cm.setFmeasure(2*cm.getPrecision()*cm.getRecall()/(cm.getPrecision()+cm.getRecall()));
    }
    
    public void calculateAveragePrecision(ArrayList listGolden, ArrayList listResponses, CalculatedMeasures cm)
    {
        double ap=0;
        
        for(int i=0;i<listResponses.size();i++)
        {
            ap+=precisionAtRfirstItems(i+1, listGolden, listResponses)*relevance(listResponses.get(i), listGolden);
        }
        
         listResponses.retainAll(listGolden);
         if(listResponses.isEmpty()){
             cm.setAverage_precision(0);
             return;
         }
	 /** URGENT: check if the division id correct **/
	 // we should divide with the size of the golden list
        
        // ** UPDATE 17/02/2015 : in BioASQ 3 we divide with 10. Please
        //    check the guidlines **
         if(VERSION_OF_CHALLENGE==EvaluatorTask1b.BIOASQ2)
            cm.setAverage_precision(ap/(double)listGolden.size());
         else if(VERSION_OF_CHALLENGE==EvaluatorTask1b.BIOASQ3 || this.VERSION_OF_CHALLENGE==evaluation.EvaluatorTask1b.BIOASQ5)
            cm.setAverage_precision(ap/10.0);
    }
    
    public double precisionAtRfirstItems(int r,ArrayList listGolden, ArrayList listResponses)
    {
        double tp=0,fp=0;
        
        if(listResponses.isEmpty())
        {
            return 0;
        }
        
        for(int i=0;i<r;i++)
        {
            Object item = listResponses.get(i);
            if(listGolden.contains(item))
                tp++;
            else
            {
                fp++;
            }
        }
        if((tp+fp)==0)
            return 0;
        return tp/(tp+fp);
    }
    
    public int relevance(Object item,ArrayList listGolden)
    {
        if(listGolden.contains(item))
            return 1;
        return 0;
    }
    
    public void calculateAveragePrecisionSnippets(ArrayList<Snippet> listGolden, 
            ArrayList<Snippet> listResponses, CalculatedMeasures cm)
    {
        double ap=0;
        for(int i=0;i<listResponses.size();i++)
        {
            ap+=precisionAtRSnippet(i+1, listGolden, listResponses)*relevanceSnippet(listResponses.get(i), listGolden);
        }
        
         // ** UPDATE 17/02/2015 : in BioASQ 3 we divide with 10. Please
        //    check the guidlines **
        if(VERSION_OF_CHALLENGE==EvaluatorTask1b.BIOASQ2)
            cm.setAverage_precision(ap/(double)listGolden.size());
        else if(VERSION_OF_CHALLENGE==EvaluatorTask1b.BIOASQ3 || this.VERSION_OF_CHALLENGE==evaluation.EvaluatorTask1b.BIOASQ5)
            cm.setAverage_precision(ap/10.0);

    }
    
    public double precisionAtRSnippet(int r,ArrayList<Snippet> listGolden, ArrayList<Snippet> listResponses)
    {
        if(listResponses.isEmpty())
        {
            return 0;
        }
        
        int resp_size=0;
        int total_overlap=0;
        int g_size=0;
        
        for(int i=0;i<r;i++)
        {
            Snippet sn = listResponses.get(i);
            resp_size += sn.getSize();
            
            for(int j=0;j<listGolden.size();j++)
            {
                Snippet g = listGolden.get(j);
                if(sn.getDocument().equals(g.getDocument()))
                {
                    total_overlap += sn.overlap(g);
                }
            }
        }
        
        for(int j=0;j<listGolden.size();j++)
         {
            Snippet g = listGolden.get(j);
            g_size+=g.getSize();
         }
        
        
        return (double)total_overlap/((double)resp_size);
    }

    private double relevanceSnippet(Snippet ret, ArrayList<Snippet> listGolden) {
        
            
            for(int j=0;j<listGolden.size();j++)
            {
                Snippet g = listGolden.get(j);
                if(ret.getDocument().equals(g.getDocument()))
                {
                    if(ret.overlap(g)!=0);
                        return 1;
                }
            }
         return 0;
    }
    
    
    
    
    public double getAveragePrecisionConcepts()
    {
        return concepts.getAverage_precision();
    }

    public double getAveragePrecisionDocuments()
    {
        return articles.getAverage_precision();
    }

    public double getAveragePrecisionTriples()
    {
        return triples.getAverage_precision();
    }

    public double getAveragePrecisionSnippets()
    {
        return snippets.getAverage_precision();
    }

    public double getF1Snippets()
    {
        return snippets.getFmeasure();
    }

    private void accuracyYesNo(ExactAnswer exact_answer, ExactAnswer response,CalculatedMeasures cm) {
	
	if(response==null||response.getAnswer().isEmpty()||response.getAnswer()==null)
	{	    
	    cm.setAccuracy(0.0);return;
	}
        if(exact_answer.getAnswer().equals(response.getAnswer()))
            cm.setAccuracy(1.0);
    }

    private void strictAccuracy(ExactAnswer gold_answer, ExactAnswer system_answer, CalculatedMeasures exact_answers) {
	if(system_answer==null)
	    return;
        ArrayList<String> answers_golden = gold_answer.getAnswers();
        ArrayList<String> answers_system = system_answer.getAnswers();
       
	if(answers_system.isEmpty()||answers_golden.isEmpty())
	{
            exact_answers.setStrict_accuracy(0.0);
	    return;
	}
        if(answers_system.get(0).equals(answers_golden.get(0)))
            exact_answers.setStrict_accuracy(1.0);
    }

    
    private void strictAccuracyForLists(ExactAnswer gold_answer, ExactAnswer system_answer, CalculatedMeasures exact_answers) {
	if(system_answer==null)
	    return;
        
        ArrayList<ArrayList<String>> listsOfFactAnswers = system_answer.getLists();
        //check for emptyness of list added 
        if(!listsOfFactAnswers.isEmpty() && gold_answer.containsAnswerSynonym(listsOfFactAnswers.get(0),false)){
                exact_answers.setStrict_accuracy(1.0);
                return;
            }
        
        
        exact_answers.setStrict_accuracy(0.0);
    }

    private void lenientAccuracyForLists(ExactAnswer gold_answer, ExactAnswer system_answer, CalculatedMeasures exact_answers) {
	if(system_answer==null)
	    return;
        
        
        ArrayList<ArrayList<String>> listsOfFactAnswers = system_answer.getLists();
        
        for(ArrayList<String> ans_system : listsOfFactAnswers)
        {
            if(gold_answer.containsAnswerSynonym(ans_system,false)){
                exact_answers.setLenient_accuracy(1.0);
                return;
            }
        }
        
        
    }

    
    private void lenientAccuracy(ExactAnswer gold_answer, ExactAnswer system_answer, CalculatedMeasures exact_answers) {
	if(system_answer==null)
	    return;
        ArrayList<String> answers_golden = gold_answer.getAnswers();
        ArrayList<String> answers_system = system_answer.getAnswers();
        
        for(int i=0;i<answers_system.size();i++){
            for(int j=0;j<answers_golden.size();j++){
                if(answers_system.get(i).equals(answers_golden.get(j)))
                {
                    exact_answers.setLenient_accuracy(1.0);
                    return;
                }
                
            }
        }
    }

    private void meanReciprocalRank(ExactAnswer gold_answer, ExactAnswer system_answer, CalculatedMeasures exact_answers) {
	if(system_answer==null)
	    return;
        ArrayList<String> answers_golden = gold_answer.getAnswers();
        ArrayList<String> answers_system = system_answer.getAnswers();
        
        for(int i=0;i<answers_system.size();i++){
            for(int j=0;j<answers_golden.size();j++){
                if(answers_system.get(i).equals(answers_golden.get(j)))
                {
                    exact_answers.setMean_reciprocal_rank(1.0/(double)(i+1));
                    //System.out.println(1.0/(double)(i+1));
                    return;
                }
                
            }
        }
    }

    private void meanReciprocalRankForLists(ExactAnswer gold_answer, ExactAnswer system_answer, CalculatedMeasures exact_answers) {
	if(system_answer==null)
	    return;
        
        ArrayList<ArrayList<String>> listsOfFactAnswers = system_answer.getLists();
        
        for(int i=0;i<listsOfFactAnswers.size();i++)
        {
            
            if(gold_answer.containsAnswerSynonym(listsOfFactAnswers.get(i),false)){
                exact_answers.setMean_reciprocal_rank(1.0/(double)(i+1));
                return;
            }
        }
        
    }

    
    public double getPrecisionEA()
    {
        return exact_answers.getPrecision();
    }

    public double getRecallEA()
    {
        return exact_answers.getRecall();
    }

    public double getF1EA()
    {
        return exact_answers.getFmeasure();
    }

    
    public double getAccuracyYesNo() {
        return exact_answers.getAccuracy();
    }

    public double getStrictAccuracy()
    {
        return exact_answers.getStrict_accuracy();
    }

    public double getLenientAccuracy()
    {
        return exact_answers.getLenient_accuracy();
    }

    public double getMRR()
    {
        return exact_answers.getMean_reciprocal_rank();
    }

    
    public int getQuestion_type() {
        return question_type;
    }
    
    public double getConceptsPrecision()
    {
        return concepts.getPrecision();
    }

    public double getConceptsRecall()
    {
        return concepts.getRecall();
    }

    public double getConceptsF1()
    {
        return concepts.getFmeasure();
    }

    
   public double getArticlesPrecision()
   {
        return articles.getPrecision();
    }

    public double getArticlesRecall()
    {
        return articles.getRecall();
    }

    public double getArticlesF1()
    {
        return articles.getFmeasure();
    }

    public double getSnippetsPrecision()
    {
        return snippets.getPrecision();
    }

    public double getSnippetsRecall()
    {
        return snippets.getRecall();
    }

    public double getSnippetsF1()
    {
        return snippets.getFmeasure();
    }

    public double getTriplesPrecision()
    {
        return triples.getPrecision();
    }

    public double getTriplesRecall()
    {
        return triples.getRecall();
    }

    public double getTriplesF1()
    {
        return triples.getFmeasure();
    }

    
    public void concatenateSnippets(ArrayList<Snippet> listsnip)
    {
        if(listsnip.isEmpty())
        {
            return;
        }
        
        
        for(int i=0;i<listsnip.size();i++)
        {
            
            for(int j=0;j<listsnip.size();j++)
            {
                if(j==i)
                    continue;
                if(listsnip.get(i).getDocument().equals(listsnip.get(j).getDocument()))
                {
                    if(listsnip.get(i).getFieldNameBegin().equals(listsnip.get(j).getFieldNameBegin())&&
                            listsnip.get(i).getFieldNameEnd().equals(listsnip.get(j).getFieldNameEnd()))
                    {
                        if(listsnip.get(i).itOverlaps(listsnip.get(j))) // merge snippets
                        {
                            Snippet merged = listsnip.get(i).merge(listsnip.get(j));
                            listsnip.remove(i);
                            listsnip.add(i, merged);
                            listsnip.remove(j);
                           // System.out.println("Merging "+listsnip.get(i).getDocument());
                           // System.out.println(merged.getBegin_index()+" "+merged.getEnd_index());
                            j=0;
                        }
                    }
                }
            }
        }
        
    }
    
    public boolean hasQuestionConcepts()
    {
        return has_concepts;
    }
    
}
