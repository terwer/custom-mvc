import org.junit.Test;

/**
 * @author: terwer
 * @date: 2021/12/24 20:16
 * @description:
 */
public class MvcTest {
    /**
     * 手写MVC框架基础上增加如下功能
     *
     * 1）定义注解@Security（有value属性，接收String数组），该注解用于添加在Controller类或者Handler方法上，表明哪些用户拥有访问该Handler方法的权限（注解配置用户名）
     *
     * 2）访问Handler时，用户名直接以参数名username紧跟在请求的url后面即可，比如http://localhost:8080/demo/handle01?username=zhangsan
     *
     * 3）程序要进行验证，有访问权限则放行，没有访问权限在页面上输出
     *
     * 注意：自己造几个用户以及url，上交作业时，文档提供哪个用户有哪个url的访问权限
     *
     * 题目分析
     *
     *
     *
     * 实现思路
     *
     * 代码讲解
     *   1）展示关键实现代码
     *
     *   2）有访问权限则放行，没有访问权限在页面上输出
     */
    @Test
    public void testMvc(){

    }
}
