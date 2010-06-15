/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.apache.lucene.search.spell;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * 
 * This SpellChecker is an original lucene spellchecker version 2.2.0 pathced with version 2.4.1 fixes.
 * 
 * Original issue: <ul><li>http://jira.exoplatform.org/browse/JCR-1405</ul>
 * 
 * Applied patches:<ul>
 * <li> https://issues.apache.org/jira/browse/LUCENE-1046
 * <li> https://issues.apache.org/jira/browse/LUCENE-1548</ul>
 * 
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: PSpellChecker.java 111 2008-11-11 11:11:11Z serg $
 */
public class ExtendedSpellChecker extends SpellChecker
{
   /**
    * Boost value for start and end grams
    */
   private float bStart = 2.0f;

   private float bEnd = 1.0f;

   private float minScore = 0.5f;

   private IndexSearcher indexSearcher;

   /**
    * Use the given directory as a spell checker index. The directory
    * is created if it doesn't exist yet.
    * 
    * @param spellIndex
    * @throws IOException
    */
   public ExtendedSpellChecker(Directory spellIndex) throws IOException
   {
      super(spellIndex);
   }

   /**
    * Use a different index as the spell checker index or re-open
    * the existing index if <code>spellIndex</code> is the same value
    * as given in the constructor.
    * 
    * @param spellIndex
    * @throws IOException
    */
   public void setSpellIndex(Directory spellIndex) throws IOException
   {
      super.setSpellIndex(spellIndex);

      if (indexSearcher != null)
      {
         indexSearcher.close();
      }
      indexSearcher = new IndexSearcher(this.spellIndex);
   }

   /**
    * Sets the accuracy 0 &lt; minScore &lt; 1; default 0.5
    */
   public void setAccuracy(float minScore)
   {
      super.setAccuracy(minScore);
      this.minScore = minScore;
   }

   /**
    * Suggest similar words.
    * 
    * <p>As the Lucene similarity that is used to fetch the most relevant n-grammed terms
    * is not the same as the edit distance strategy used to calculate the best
    * matching spell-checked word from the hits that Lucene found, one usually has
    * to retrieve a couple of numSug's in order to get the true best match.
    *
    * <p>I.e. if numSug == 1, don't count on that suggestion being the best one.
    * Thus, you should set this value to <b>at least</b> 5 for a good suggestion.
    *
    * @param word the word you want a spell check done on
    * @param numSug the number of suggested words
    * @throws IOException
    * @return String[]
    */
   public String[] suggestSimilar(String word, int numSug) throws IOException
   {
      return this.suggestSimilar(word, numSug, null, null, false);
   }

   /**
    * Suggest similar words (optionally restricted to a field of an index).
    * 
    * <p>As the Lucene similarity that is used to fetch the most relevant n-grammed terms
    * is not the same as the edit distance strategy used to calculate the best
    * matching spell-checked word from the hits that Lucene found, one usually has
    * to retrieve a couple of numSug's in order to get the true best match.
    *
    * <p>I.e. if numSug == 1, don't count on that suggestion being the best one.
    * Thus, you should set this value to <b>at least</b> 5 for a good suggestion.
    *
    * @param word the word you want a spell check done on
    * @param numSug the number of suggested words
    * @param ir the indexReader of the user index (can be null see field param)
    * @param field the field of the user index: if field is not null, the suggested
    * words are restricted to the words present in this field.
    * @param morePopular return only the suggest words that are more frequent than the searched word
    * (only if restricted mode = (indexReader!=null and field!=null)
    * @throws IOException
    * @return String[] the sorted list of the suggest words with these 2 criteria:
    * first criteria: the edit distance, second criteria (only if restricted mode): the popularity
    * of the suggest words in the field of the user index
    */
   public String[] suggestSimilar(String word, int numSug, IndexReader ir, String field, boolean morePopular)
      throws IOException
   {

      float min = this.minScore;
      final TRStringDistance sd = new TRStringDistance(word);
      final int lengthWord = word.length();

      //final int goalFreq = (morePopular && ir != null) ? ir.docFreq(new Term(field, word)) : 0;
      final int freq = (ir != null && field != null) ? ir.docFreq(new Term(field, word)) : 0;
      final int goalFreq = (morePopular && ir != null && field != null) ? freq : 0;
      // if the word exists in the real index and we don't care for word frequency, return the word itself
      //if (!morePopular && goalFreq > 0)
      if (!morePopular && freq > 0)
      {
         return new String[]{word};
      }

      BooleanQuery query = new BooleanQuery();
      String[] grams;
      String key;

      for (int ng = getMin(lengthWord); ng <= getMax(lengthWord); ng++)
      {

         key = "gram" + ng; // form key

         grams = formGrams(word, ng); // form word into ngrams (allow dups too)

         if (grams.length == 0)
         {
            continue; // hmm
         }

         if (bStart > 0)
         { // should we boost prefixes?
            add(query, "start" + ng, grams[0], bStart); // matches start of word

         }
         if (bEnd > 0)
         { // should we boost suffixes
            add(query, "end" + ng, grams[grams.length - 1], bEnd); // matches end of word

         }
         for (int i = 0; i < grams.length; i++)
         {
            add(query, key, grams[i]);
         }
      }

      //     System.out.println("Q: " + query);
      Hits hits = indexSearcher.search(query);
      //     System.out.println("HITS: " + hits.length());
      SuggestWordQueue sugQueue = new SuggestWordQueue(numSug);

      // go thru more than 'maxr' matches in case the distance filter triggers
      int stop = Math.min(hits.length(), 10 * numSug);
      SuggestWord sugWord = new SuggestWord();
      for (int i = 0; i < stop; i++)
      {

         sugWord.string = hits.doc(i).get(F_WORD); // get orig word

         // don't suggest a word for itself, that would be silly
         if (sugWord.string.equals(word))
         {
            continue;
         }

         // edit distance/normalize with the minScore word length
         sugWord.score = 1.0f - ((float)sd.getDistance(sugWord.string) / Math.max(sugWord.string.length(), lengthWord));
         if (sugWord.score < min)
         {
            continue;
         }

         if (ir != null)
         { // use the user index
            sugWord.freq = ir.docFreq(new Term(field, sugWord.string)); // freq in the index
            // don't suggest a word that is not present in the field
            if ((morePopular && goalFreq > sugWord.freq) || sugWord.freq < 1)
            {
               continue;
            }
         }
         sugQueue.insert(sugWord);
         if (sugQueue.size() == numSug)
         {
            // if queue full, maintain the minScore score
            min = ((SuggestWord)sugQueue.top()).score;
         }
         sugWord = new SuggestWord();
      }

      // convert to array string
      String[] list = new String[sugQueue.size()];
      for (int i = sugQueue.size() - 1; i >= 0; i--)
      {
         list[i] = ((SuggestWord)sugQueue.pop()).string;
      }

      return list;
   }

   /**
    * Add a clause to a boolean query.
    */
   private static void add(BooleanQuery q, String name, String value, float boost)
   {
      Query tq = new TermQuery(new Term(name, value));
      tq.setBoost(boost);
      q.add(new BooleanClause(tq, BooleanClause.Occur.SHOULD));
   }

   /**
    * Add a clause to a boolean query.
    */
   private static void add(BooleanQuery q, String name, String value)
   {
      q.add(new BooleanClause(new TermQuery(new Term(name, value)), BooleanClause.Occur.SHOULD));
   }

   /**
    * Form all ngrams for a given word.
    * @param text the word to parse
    * @param ng the ngram length e.g. 3
    * @return an array of all ngrams in the word and note that duplicates are not removed
    */
   private static String[] formGrams(String text, int ng)
   {
      int len = text.length();
      String[] res = new String[len - ng + 1];
      for (int i = 0; i < len - ng + 1; i++)
      {
         res[i] = text.substring(i, i + ng);
      }
      return res;
   }

   /**
    * Index a Dictionary
    * @param dict the dictionary to index
    * @throws IOException
    */
   public void indexDictionary(Dictionary dict) throws IOException
   {

      super.indexDictionary(dict);
      indexSearcher.close();
      indexSearcher = new IndexSearcher(this.spellIndex);
   }

   private int getMin(int l)
   {
      if (l > 5)
      {
         return 3;
      }
      if (l == 5)
      {
         return 2;
      }
      return 1;
   }

   private int getMax(int l)
   {
      if (l > 5)
      {
         return 4;
      }
      if (l == 5)
      {
         return 3;
      }
      return 2;
   }
}
