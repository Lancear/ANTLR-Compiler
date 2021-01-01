package information;

public class Information {

  public <T extends Information> boolean is(Class<T> clazz) {
    return clazz.isInstance(this);
  }

  public <T extends Information> T as(Class<T> clazz) {
    return clazz.cast(this);
  }

}
