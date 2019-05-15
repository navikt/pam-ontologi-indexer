package no.nav.arbeid.pam.ontologindexer.client;

import java.io.IOException;
import java.util.List;

public interface EsIndexerService {

  void index(ESObject esObject, Class clazz) throws IOException;

  int bulkIndex(List<ESObject> esCver, Class clazz) throws IOException;

  void createIndex(Class clazz) throws IOException;

  void deleteIndex(Class clazz) throws IOException;

  boolean doesIndexExist(Class clazz) throws IOException;

  long antallIndeksert(Class clazz);
}