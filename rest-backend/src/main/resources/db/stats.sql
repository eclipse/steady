-- Project count
-- Number of distinct group identifiers (minus obvious tests)
select count(distinct mvn_group) from app where not (mvn_group like 'com.acme%' or mvn_group like 'VulasTest' or mvn_group like 'test' or mvn_group like 'com.test' or mvn_group like 'cf-helloworld' or mvn_group like 'com-helloworld' or artifact = 'SAPGoatStore');

-- Active project count
-- Number of distinct group identifiers for which goals have been executed in a given time frame
select count(distinct p.mvn_group) from 
	(select distinct g.created_at,a.mvn_group from app_goal_exe g join app a on g.app=a.id 
		where g.created_at  >= '2018-10-01' and  g.created_at  < '2018-11-01' ) as p 

-- Number of goals executions in a given time frame
select count(*) from app_goal_exe as g join app a on a.id=g.app where g.goal='APP'  
		and g.created_at  >= '2020-04-01' and  g.created_at  < '2020-04-30'
