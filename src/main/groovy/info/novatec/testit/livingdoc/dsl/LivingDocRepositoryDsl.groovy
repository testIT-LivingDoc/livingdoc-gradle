package info.novatec.testit.livingdoc.dsl

class LivingDocRepositoryDsl {
  
  public String name
  
  public String implementation

  public String url

  public String uid

  public String filter = /.*/
  
  LivingDocRepositoryDsl(String name) {
    this.name = name
  }
}
