% ====== Kumar parameter tuning
bbp = [0.0002, 0.0004, 0.0006, 0.0008, 0.001, 0.0015, 0.002];
k= [0.1, 0.2, 0.3, 0.5, 0.8, 1];
mean_ber = mean(reshape(readmatrix('results/KumarParameterTuning_BER_7_6_8.txt'),7, 6, 8), 3);
mean_psnr = mean(reshape(readmatrix('results/KumarParameterTuning_PSNR_7_6_8.txt'),7, 6, 8), 3);
mean_ssim = mean(reshape(readmatrix('results/KumarParameterTuning_SSIM_7_6_8.txt'),7, 6, 8), 3);

colors = [[0 0.4470 0.7410]; [0.8500 0.3250 0.0980];
    [0.9290 0.6940 0.1250]; [0.4940 0.1840 0.5560]; [0.4660 0.6740 0.1880];
    [0.3010 0.7450 0.9330];[0.6350 0.0780 0.1840]];


% ====== C4S parameter tuning
cr = 2:12;
rhos = [0.01, 0.03, 0.05, 0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 2];
epsilons = [0.004, 0.006, 0.008, 0.01, 0.012, 0.014, 0.016];
psnr = [47.68514751944914, 47.67324297492957, 47.67475815576891, 47.66094208605836, 47.535805279984466, 46.90087260277228, 44.248372549930934, 40.00913767431743, 34.47115108385498, 28.377849050444198, 21.93116826798189];
ssim = [0.9850109392659154, 0.9850862519600869, 0.9850746241304964, 0.9850003863829646, 0.9842396760275793, 0.9816640057795002, 0.9711483395073728, 0.9507539580052179, 0.895660008355127, 0.7890454172402496, 0.5527652690213293, ];
%h = zeros(2,1); % initialize handles for 11 plots
figure;
set(gcf,'position',[100,100,350,300]);
h = plot(cr,ssim,'-o','color',colors(1,:), 'LineWidth', 1.5);
%h(1) = plot(cr,ssim,'-o','color',colors(1,:), 'LineWidth', 1.5);
%hold on;
%for i = 2 : 7
  %h(i)=plot(rhos, psnr(:, i),'-o','color',colors(i,:), 'LineWidth',1.5);
%end
%h(2) = plot(cr,ones(1,11)*40,'--','color','red', 'LineWidth',1.5);
hold off;
ax = gca;
ax.FontSize = 12;
xlabel('compression rate', 'FontSize',12)
ylabel('SSIM', 'FontSize',12)
title('C_4S with Gold Code', 'FontSize',14)
xlim tight
legend('\rho = 0.01, \epsilon=0.004');



