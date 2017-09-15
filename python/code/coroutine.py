# -*- encoding:utf8 -*-
"""
    author: quanbin_zhu
    time  : 2017/9/14 11:54
    SELECT device_list.mac, common_config.svalue,instore from device_list,common_config  WHERE device_list.shop_id=common_config.skey and device_list.mac in (SELECT mac from device_list WHERE instore!=0 and shop_id in (138041));
    
    并发(concurrent) & 并行（parallel）：
        【引用Erlang之父 - Joe Armstrong】https://joearms.github.io/published/2013-04-05-concurrent-and-parallel-programming.html
        涉及到多线程和多进程编程，很多人都统称为并发或并行， 其实两者是有区别的：
        并发：可以理解为多个线程在单核上交替运行，每个线程都会分得一定的CPU时间片；
        并行：可以理解为多个线程在多核上同时运行，每个线程都会独享一个CPU；
    
    
    线程：
        Python的多线程在某种程度上讲是比较尴尬的存在，受制于Python的GIL（Global Interpreter Lock）全局解释器锁，阻止Python代码同时在多个处理器核心上运行，因为一个Python解释器在同一时刻只能运行于一个处理器之中。
        1、对于CPU密集型计算， 并不能算真正的并行计算，不能发挥多核的作用；
        2、对于IO 密集型（如爬虫等），由于GIL的原因，虽然在性能上优于单线程，但多线程的切换， 资源的申请释放 成本开销同样也是不可忽略的。
    
    协程：
        在线程与协程之间，个人更偏向于协程，
        1、线程资源开销远大于协程， 线程它有自己的线程空间，在linux下可以理解为一个轻量级进程；
        2、线程的上下文切换可能会引起寄存器和内存间的复制拷贝等；协程在进行切换时，是代码块进行切换（栈跳转），可以更多的利用CPU时间片；
        
"""
import gevent
from gevent import monkey;monkey.patch_all()
from gevent.pool import Pool
from functools import partial

class Task(gevent.Greenlet):
    def __init__(self, id):
        super(Task, self).__init__()
        self.task_id = id

    def _run(self):
        for index in xrange(1, 6):
            print "task id %s,  output value %s" % (self.task_id, index)
            gevent.sleep(0)

def show(id, f, t):
    for index in xrange(f, t):
        print "task id %s,  output value %s" % (id, index)
        gevent.sleep(0)

def func_dispatch():
    func = partial(show, f = 1, t=3)
    g1 = gevent.spawn(func, "gevent-01")
    g1.join()

def class_dispatch():
    tasks = [ Task(i) for i in xrange(0,3)]
    map(lambda t: t.start(), tasks)
    map(lambda t: t.join(), tasks)

def class_dispatch_with_pool():
    tasks = [Task(i) for i in xrange(0, 3)]
    pools = Pool(2)
    map(lambda t: pools.start(t), tasks)
    pools.join()

if __name__ == '__main__':
    func_dispatch()

