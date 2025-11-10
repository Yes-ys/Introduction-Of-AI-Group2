package stud.g01.pdb;
/**
 *  不相交模式数据库接口
 *  获取和插入模式数据库中的数据
 *
 */
public interface PdbI {
    /**
     * 根据模式ID和状态key，获取启发值
     * @param
     *      patternId 模式ID
     *      key  状态key
     * @return 该状态的启发值
     */
    Integer getCost(Integer patternId, String key);
    /**
     * Insert模式ID和状态key，插入启发值
     * @param
     *      patternId 模式ID
     *      key  状态key
     *      cost  启发值
     */
    void InsertPatternId(Integer patternId, String key, Integer cost);

}
