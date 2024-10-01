package com.kos.datacache

import java.time.OffsetDateTime

object TestHelper {

    private val cache = """{                                
     "id": 1,                    
     "name": "Proassassin",       
     "spec": "Havoc",             
     "class": "Demon Hunter",     
     "score": 0.0,                
     "quantile": 0.0,             
     "mythicPlusRanks": {         
         "class": {               
             "realm": 0,          
             "world": 0,          
             "region": 0          
         },                       
         "specs": [],                       
         "overall": {             
             "realm": 0,          
             "world": 0,          
             "region": 0          
         }                        
     },                           
     "mythicPlusBestRuns": [],
     "type": "com.kos.httpclients.domain.RaiderIoData"
   }"""

    private val now = OffsetDateTime.now()
    val outdatedDataCache = DataCache(1, cache, now.minusHours(25))
    val dataCache = DataCache(1, cache, now)
}