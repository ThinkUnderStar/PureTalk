package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.Report;
import thinkunderstar.puretalk.puretalkbackend.mapper.ReportMapper;
import thinkunderstar.puretalk.puretalkbackend.service.ReportService;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
}
