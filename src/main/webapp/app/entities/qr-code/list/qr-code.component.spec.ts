import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { QRCodeService } from '../service/qr-code.service';

import { QRCodeComponent } from './qr-code.component';

describe('QRCode Management Component', () => {
  let comp: QRCodeComponent;
  let fixture: ComponentFixture<QRCodeComponent>;
  let service: QRCodeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'qr-code', component: QRCodeComponent }]),
        HttpClientTestingModule,
        QRCodeComponent,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              }),
            ),
            snapshot: { queryParams: {} },
          },
        },
      ],
    })
      .overrideTemplate(QRCodeComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(QRCodeComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(QRCodeService);

    const headers = new HttpHeaders();
    jest.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 123 }],
          headers,
        }),
      ),
    );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.qRCodes?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to qRCodeService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getQRCodeIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getQRCodeIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
